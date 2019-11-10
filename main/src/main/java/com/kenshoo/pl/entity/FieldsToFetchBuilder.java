package com.kenshoo.pl.entity;

import com.kenshoo.pl.entity.spi.ChangeOperationSpecificConsumer;
import com.kenshoo.pl.entity.spi.CurrentStateConsumer;
import org.jooq.lambda.Seq;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.kenshoo.pl.entity.ChangeOperation.*;
import static com.kenshoo.pl.entity.FieldFetchRequest.newRequest;
import static java.util.stream.Collectors.toList;
import static org.jooq.lambda.Seq.duplicate;
import static org.jooq.lambda.Seq.seq;

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

        final Stream<EntityField<?, ?>> fields = Stream.concat(
                currentStateConsumers.flatMap(
                        consumer -> validateFieldsToFetch(flow, operation, consumer.getRequiredFields(commands, operation), consumer)
                ),
                fieldsOf(commands, operation));

        return seq(fields).flatMap(field -> {
            if (isSameLevel(flow, field)) {
                return Seq.of(newRequest().field(field).queryOn(currentLevel).askedBy(currentLevel).build());
            } else if (hierarchy.contains(field)) {
                return Seq.of(newRequest().field(field).queryOn(field.getEntityType()).askedBy(currentLevel).build());
            } else {
                return Seq.of(newRequest().field(field).queryOn(hierarchy.root()).askedBy(currentLevel).build());
            }
        });
    }

    private <E extends EntityType<E>> Stream<EntityField<E, ?>> fieldsOf(Collection<? extends ChangeEntityCommand<E>> commands, ChangeOperation operation) {
        if(!commands.isEmpty() && SupportedChangeOperation.UPDATE_AND_DELETE.supports(operation)) {
            ChangeEntityCommand<E> cmd = commands.stream().findFirst().get();
            return hasAnyChildCommand(commands) ? Stream.concat(uniqueFields(cmd), primaryKeyFields(cmd)) : uniqueFields(cmd);
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

    private <E extends EntityType<E>, EF extends EntityField<?, ?>> Stream<EF> validateFieldsToFetch(ChangeFlowConfig<E> flowConfig, ChangeOperation changeOperation, Stream<EF> fieldsToFetch, CurrentStateConsumer<E> consumer) {
        if(changeOperation == CREATE) {
            return fieldsToFetch.filter(entityField -> {
                if (entityField.getDbAdapter().getTable().equals(flowConfig.getEntityType().getPrimaryTable())) {
                    throw new IllegalStateException("Field " + entityField + " of the entity type: " + entityField.getEntityType() + " and primary table is requested in CREATE flow by " + consumer);
                }
                return true;
            });
        } else {
            return fieldsToFetch;
        }
    }



}
