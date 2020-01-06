package com.kenshoo.pl.entity;

import com.kenshoo.pl.entity.spi.ChangeOperationSpecificConsumer;
import com.kenshoo.pl.entity.spi.CurrentStateConsumer;
import com.kenshoo.pl.entity.spi.PostFetchCommandEnricher;
import org.jooq.lambda.Seq;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.kenshoo.pl.entity.ChangeOperation.*;
import static com.kenshoo.pl.entity.FieldFetchRequest.newRequest;
import static java.util.stream.Collectors.toList;
import static org.jooq.lambda.Seq.seq;
import static org.jooq.lambda.function.Functions.not;

public class FieldsToFetchBuilder<ROOT extends EntityType<ROOT>> {

    public Collection<FieldFetchRequest> build(Collection<? extends ChangeEntityCommand<ROOT>> commands, ChangeFlowConfig<ROOT> flowConfig) {
        return prepareFieldsToFetchRecursive(Hierarchy.build(flowConfig), commands, flowConfig)
                .toList();
    }

    private <E extends EntityType<E>> Seq<FieldFetchRequest> prepareFieldsToFetchRecursive(
            Hierarchy hierarchy,
            Collection<? extends ChangeEntityCommand<E>> commands,
            ChangeFlowConfig<E> flow) {

        return Seq.concat(
                getForOneLevel(hierarchy, only(commands, withOperator(UPDATE)), UPDATE, flow),
                getForOneLevel(hierarchy, only(commands, withOperator(CREATE).or(withAllowMissingEntity())), CREATE, flow),
                getForOneLevel(hierarchy, only(commands, withOperator(DELETE)), DELETE, flow),
                seq(flow.childFlows()).flatMap(childFlow -> prepareChildFieldsToFetchRecursive(hierarchy, commands, childFlow))
        );
    }

    private <PARENT extends EntityType<PARENT>, CHILD extends EntityType<CHILD>> Seq<FieldFetchRequest> prepareChildFieldsToFetchRecursive(Hierarchy hierarchy, Collection<? extends ChangeEntityCommand<PARENT>> commands, ChangeFlowConfig<CHILD> childFlow) {
        return prepareFieldsToFetchRecursive(hierarchy, commands.stream().flatMap(parent -> parent.getChildren(childFlow.getEntityType())).collect(toList()), childFlow);
    }

    private <E extends EntityType<E>> Seq<FieldFetchRequest> getForOneLevel(
            Hierarchy hierarchy,
            Collection<? extends ChangeEntityCommand<E>> commands,
            ChangeOperation operation,
            ChangeFlowConfig<E> flow) {

        if (commands.isEmpty()) {
            return Seq.empty();
        }

        final EntityType<E> currentLevel = flow.getEntityType();

        final Stream<CurrentStateConsumer<E>> currentStateConsumers =
                Stream.concat(flow.currentStateConsumers(), consumerOf(commands))
                        .filter(onlyConsumersWith(operation));

        final Seq<EntityField<?, ?>> fields = Seq.concat(
                flow.getFeatures().isEnabled(Feature.RequiredFieldsNewApi) ? fieldsConsumedBy(commands, operation, flow, currentStateConsumers) : fieldsConsumedByDeprecated(commands, operation, flow, currentStateConsumers),
                fieldsRelatedByChildrenOf(commands, operation),
                fieldsOfIdentifiersOf(commands, operation));

        return fields.flatMap(field -> {
            if (isSameLevel(flow, field)) {
                return Seq.of(newRequest().field(field).queryOn(currentLevel).askedBy(currentLevel).build());
            } else if (hierarchy.contains(field)) {
                return Seq.of(newRequest().field(field).queryOn(field.getEntityType()).askedBy(currentLevel).build());
            } else {
                return Seq.of(newRequest().field(field).queryOn(hierarchy.root()).askedBy(currentLevel).build());
            }
        });
    }

    private <E extends EntityType<E>> Stream<? extends EntityField<?, ?>> fieldsConsumedByDeprecated(Collection<? extends ChangeEntityCommand<E>> commands, ChangeOperation operation, ChangeFlowConfig<E> flow, Stream<CurrentStateConsumer<E>> currentStateConsumers) {
        return currentStateConsumers.flatMap(
                consumer -> filterFieldsByOperator(flow, operation, consumer.getRequiredFields(commands, operation), consumer)
        );
    }

    private <E extends EntityType<E>> Stream<? extends EntityField<?, ?>> fieldsConsumedBy(Collection<? extends ChangeEntityCommand<E>> commands, ChangeOperation operation, ChangeFlowConfig<E> flow, Stream<CurrentStateConsumer<E>> currentStateConsumers) {
        Stream<EntityField<E, ?>> fieldsByCommands = commands.stream().flatMap(ChangeEntityCommand::getChangedFields);
        Stream<EntityField<E, ?>> fieldsToEnrich = flow.getPostFetchCommandEnrichers().stream().filter(enricher -> enricher.shouldRun(commands)).flatMap(PostFetchCommandEnricher::fieldsToEnrich);
        Set<EntityField<E, ?>> fieldsToUpdate = Stream.concat(fieldsByCommands, fieldsToEnrich).collect(Collectors.toSet());
        return currentStateConsumers.flatMap(
                consumer -> filterFieldsByOperator(flow, operation, consumer.requiredFields(fieldsToUpdate, operation), consumer)
        );
    }

    private <E extends EntityType<E>> Stream<EntityField<E, ?>> fieldsOfIdentifiersOf(Collection<? extends ChangeEntityCommand<E>> commands, ChangeOperation operation) {
        if(!commands.isEmpty() && SupportedChangeOperation.UPDATE_AND_DELETE.supports(operation)) {
            ChangeEntityCommand<E> cmd = commands.stream().findFirst().get();
            return uniqueFields(cmd);
        } else {
            return Stream.empty();
        }
    }

    private <E extends EntityType<E>> Stream<EntityField<E, ?>> fieldsRelatedByChildrenOf(Collection<? extends ChangeEntityCommand<E>> commands, ChangeOperation operation) {
        if(SupportedChangeOperation.UPDATE_AND_DELETE.supports(operation) && hasAnyChildCommand(commands)) {
            ChangeEntityCommand<E> cmd = commands.stream().findFirst().get();
            return primaryKeyFields(cmd);
        } else {
            return Stream.empty();
        }
    }

    private <E extends EntityType<E>> Stream<EntityField<E, ?>> uniqueFields(ChangeEntityCommand<E> cmd) {
        return Arrays.stream(cmd.getIdentifier().getUniqueKey().getFields());
    }

    private <E extends EntityType<E>> Stream<EntityField<E, ?>> primaryKeyFields(ChangeEntityCommand<E> cmd) {
        EntityType<E> entityType = cmd.getEntityType();
        return entityType.findFields(entityType.getPrimaryTable().getPrimaryKey().getFields()).stream();
    }

    private <E extends EntityType<E>> boolean hasAnyChildCommand(Collection<? extends ChangeEntityCommand<E>> commands) {
        return commands.stream().flatMap(ChangeEntityCommand::getChildren).findAny().isPresent();
    }

    private <E extends EntityType<E>> boolean isSameLevel(ChangeFlowConfig<E> flow, EntityField<?, ?> field) {
        return flow.getEntityType().equals(field.getEntityType());
    }

    private <T> List<? extends T> only(Iterable<? extends T> items, Predicate<? super T> predicate) {
        return seq(items).filter(predicate).toList();
    }

    private Predicate<EntityChange<?>> withOperator(ChangeOperation op) {
        return cmd -> op == cmd.getChangeOperation();
    }

    private Predicate<? super EntityChange<?>> withAllowMissingEntity() {
        return EntityChange::allowMissingEntity;
    }

    private <E extends EntityType<E>> Stream<CurrentStateConsumer<E>> consumerOf(Collection<? extends ChangeEntityCommand<E>> commands) {
        return commands.stream()
                .flatMap(ChangeEntityCommand::getCurrentStateConsumers);
    }

    private <E extends EntityType<E>> Predicate<CurrentStateConsumer<E>> onlyConsumersWith(ChangeOperation changeOperation) {
        return input -> {
            boolean isOperationSpecificConsumer = input instanceof ChangeOperationSpecificConsumer;
            return !isOperationSpecificConsumer || ((ChangeOperationSpecificConsumer<E>) input).getSupportedChangeOperation().supports(changeOperation);
        };
    }

    private <E extends EntityType<E>, EF extends EntityField<?, ?>> Stream<EF> filterFieldsByOperator(ChangeFlowConfig<E> flowConfig, ChangeOperation changeOperation, Stream<EF> fieldsToFetch, CurrentStateConsumer<E> consumer) {
        if(changeOperation == CREATE) {
            return fieldsToFetch.filter(not(ofEntity(flowConfig.getEntityType())));
        } else {
            return fieldsToFetch;
        }
    }

    private <E extends EntityType<E>, EF extends EntityField<?, ?>> Predicate<EF> ofEntity(EntityType<E> entityType) {
        return entityField -> entityField.getEntityType().equals(entityType);
    }


}
