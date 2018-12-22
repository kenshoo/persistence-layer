package com.kenshoo.pl.entity;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.kenshoo.pl.entity.internal.ChangesFilter;
import com.kenshoo.pl.entity.internal.EntitiesToContextFetcher;
import com.kenshoo.pl.entity.internal.EntityDbUtil;
import com.kenshoo.pl.entity.internal.RequiredFieldsCommandsFilter;
import com.kenshoo.pl.entity.internal.validators.ValidationFilter;
import com.kenshoo.pl.entity.spi.ChangeOperationSpecificConsumer;
import com.kenshoo.pl.entity.spi.CurrentStateConsumer;
import com.kenshoo.pl.entity.spi.OutputGenerator;
import com.kenshoo.pl.entity.spi.ValidationException;
import org.jooq.lambda.Seq;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.kenshoo.pl.entity.ChangeOperation.*;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.jooq.lambda.Seq.seq;

@Component
public class PersistenceLayer<ROOT extends EntityType<ROOT>> {

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private EntitiesToContextFetcher entitiesToContextFetcher;

    public void makeChanges(Collection<? extends ChangeEntityCommand<ROOT>> commands, ChangeContext context, ChangeFlowConfig<ROOT> flowConfig) {
        prepareRecursive(commands, context, flowConfig);
        Collection<? extends ChangeEntityCommand<ROOT>> validCmds = seq(commands).filter(cmd -> isValidRecursive(cmd, context, flowConfig)).toList();
        if (!validCmds.isEmpty()) {
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    generateOutputRecursive(flowConfig, validCmds, context);
                }
            });
        }
    }

    private <E extends EntityType<E>> boolean isValidRecursive(ChangeEntityCommand<E> cmd, ChangeContext context, ChangeFlowConfig<E> flow) {
        if (context.containsError(cmd)) {
            return false;
        }
        for (ChangeFlowConfig<? extends EntityType<?>> childFlow : flow.childFlows()) {
            if (!isValidChildren(cmd, context, childFlow)) {
                return false;
            }
        }
        return true;
    }

    private <PARENT extends EntityType<PARENT>, CHILD extends EntityType<CHILD>> boolean isValidChildren(ChangeEntityCommand<PARENT> cmd, ChangeContext context, ChangeFlowConfig<CHILD> childFlow) {
        return cmd.getChildren(childFlow.getEntityType()).allMatch(childCmd -> isValidRecursive(childCmd, context, childFlow));
    }

    private <E extends EntityType<E>> void prepareRecursive(
            Collection<? extends ChangeEntityCommand<E>> commands,
            ChangeContext context,
            ChangeFlowConfig<E> flow) {

        prepareOneLayer(only(commands, withOperator(DELETE)), DELETE, context, flow);
        prepareOneLayer(only(commands, withOperator(UPDATE)), UPDATE, context, flow);

        commands.stream()
                .filter(cmd -> cmd.allowMissingEntity() && isMissing(cmd, context))
                .forEach(cmd -> cmd.updateOperator(CREATE));

        prepareOneLayer(only(commands, withOperator(CREATE)), CREATE, context, flow);

        List<? extends ChangeEntityCommand<E>> validChanges = seq(commands).filter(cmd -> !context.containsError(cmd)).toList();

        flow.childFlows().forEach(childFlow -> populateKeyToParent(validChanges, childFlow, context));

        // invoke recursive
        flow.childFlows().forEach(childFlow -> prepareChildFlowRecursive(validChanges, childFlow, context));
    }

    private <PARENT extends EntityType<PARENT>, CHILD extends EntityType<CHILD>> void prepareChildFlowRecursive(List<? extends ChangeEntityCommand<PARENT>> validChanges, ChangeFlowConfig<CHILD> childFlow, ChangeContext context) {
        prepareRecursive(validChanges.stream().flatMap(parent -> parent.getChildren(childFlow.getEntityType())).collect(toList()), context, childFlow);
    }

    private <PARENT extends EntityType<PARENT>, CHILD extends EntityType<CHILD>>
    void populateKeyToParent(
            Collection<? extends ChangeEntityCommand<PARENT>> parents,
            ChangeFlowConfig<CHILD> childFlow,
            ChangeContext context) {

        if (parents.isEmpty()) {
            return;
        }

        CHILD childType = childFlow.getEntityType();

        final EntityType.ForeignKey<CHILD, PARENT> childToParent = childType.getKeyTo(first(parents).getEntityType());

        parents.forEach(parent -> {
            final Object[] values = parent.getChangeOperation() == CREATE ? EntityDbUtil.getFieldValues(childToParent.to, parent) : EntityDbUtil.getFieldValues(childToParent.to, context.getEntity(parent));
            if (childToParent.to.size() != values.length) {
                throw new IllegalStateException("Found " + values.length + " values of " + childToParent.to.size() + " fields for foreign key from table " + childType.getPrimaryTable().getName());
            }
            final UniqueKeyValue<CHILD> keysToParent = new UniqueKeyValue<>(new UniqueKey<>(array(childToParent.from)), values);
            parent.getChildren(childType).forEach(child -> child.setKeysToParent(keysToParent));
        });
    }

    private <T> T first(Iterable<T> items) {
        return items.iterator().next();
    }

    private <CHILD extends EntityType<CHILD>> EntityField<CHILD, ?>[] array(Collection<EntityField<CHILD, ?>> childFields) {
        return childFields.toArray(new EntityField[childFields.size()]);
    }

    private boolean isMissing(ChangeEntityCommand<?> cmd, ChangeContext context) {
        return context.getEntity(cmd) == Entity.EMPTY;
    }

    private <E extends EntityType<E>> Collection<EntityChange<E>> prepareOneLayer(Collection<? extends ChangeEntityCommand<E>> commands, ChangeOperation changeOperation, ChangeContext changeContext, ChangeFlowConfig<E> flowConfig) {

        if (commands.isEmpty()) {
            return emptyList();
        }

        if (!flowConfig.getEntityType().getSupportedOperation().supports(changeOperation)) {
            throw new IllegalStateException("Operation " + changeOperation + " is not supported by entity type");
        }

        commands = filterCommands(commands, flowConfig, changeOperation, changeContext);

        if (commands.isEmpty()) {
            return emptyList();
        }

        Stream<CurrentStateConsumer<E>> currentStateConsumers =
                 Stream.concat(flowConfig.currentStateConsumers(), consumerOf(commands))
                .filter(onlyConsumersWith(changeOperation));

        Stopwatch stopwatch = Stopwatch.createStarted();
        fetchEntities(commands, currentStateConsumers, changeOperation, changeContext, flowConfig);
        changeContext.getStats().addFetchTime(stopwatch.elapsed(TimeUnit.MILLISECONDS));

        commands = filterCommands(commands, getSupportedFilters(flowConfig.getPostFetchFilters(), changeOperation), changeOperation, changeContext);
        commands = resolveSuppliersAndFilterErrors(commands, changeContext);
        commands = filterCommands(commands, getSupportedFilters(flowConfig.getPostSupplyFilters(), changeOperation), changeOperation, changeContext);
        enrichCommandsPostFetch(commands, flowConfig, changeOperation, changeContext);

        return validateChanges(commands, new ValidationFilter<>(flowConfig.getValidators()), changeOperation, changeContext);
    }

    private <E extends EntityType<E>> Stream<CurrentStateConsumer<E>> consumerOf(Collection<? extends ChangeEntityCommand<E>> commands) {
        return commands.stream()
                    .flatMap(ChangeEntityCommand::getCurrentStateConsumers);
    }

    private <E extends EntityType<E>, C extends ChangeEntityCommand<E>> Collection<C> resolveSuppliersAndFilterErrors(Collection<C> commands, ChangeContext changeContext) {
        List<C> validCommands = Lists.newArrayListWithCapacity(commands.size());

        for (C command : commands) {
            Entity entity = changeContext.getEntity(command);
            try {
                command.resolveSuppliers(entity);
                validCommands.add(command);
            } catch (ValidationException e) {
                changeContext.addValidationError(command, e.getValidationError());
            }
        }

        return validCommands;
    }

    private <E extends EntityType<E>> Predicate<CurrentStateConsumer<E>> onlyConsumersWith(ChangeOperation changeOperation) {
        return input -> {
            boolean isOperationSpecificConsumer = input instanceof ChangeOperationSpecificConsumer;
            return !isOperationSpecificConsumer || ((ChangeOperationSpecificConsumer<E>) input).getSupportedChangeOperation().supports(changeOperation);
        };
    }

    private <E extends EntityType<E>, C extends ChangeEntityCommand<E>> Collection<C> filterCommands(Collection<C> commands, ChangeFlowConfig<E> flowConfig, ChangeOperation changeOperation, ChangeContext changeContext) {
        if(changeOperation == CREATE) {
            return Lists.newArrayList(new RequiredFieldsCommandsFilter<>(flowConfig.getRequiredRelationFields()).filter(commands, changeContext));
        } else {
            return commands;
        }
    }

    private <E extends EntityType<E>> void enrichCommandsPostFetch(Collection<? extends ChangeEntityCommand<E>> commands, ChangeFlowConfig<E> flowConfig, ChangeOperation changeOperation, ChangeContext changeContext) {
        flowConfig.getPostFetchCommandEnrichers().stream()
                .filter(enricher -> enricher.getSupportedChangeOperation().supports(changeOperation))
                .forEach(enricher -> enricher.enrich(commands,  changeOperation, changeContext));
    }


    private <E extends EntityType<E>> List<ChangesFilter<E>> getSupportedFilters(List<ChangesFilter<E>> filters, ChangeOperation changeOperation) {
        return filters.stream().filter(f -> f.getSupportedChangeOperation().supports(changeOperation)).collect(toList());
    }

    private <E extends EntityType<E>> void fetchEntities(Collection<? extends ChangeEntityCommand<E>> commands, Stream<CurrentStateConsumer<E>> currentStateConsumers, ChangeOperation changeOperation, ChangeContext changeContext, ChangeFlowConfig<E> flowConfig) {
        // Make sure each command is mapped to entity, even if SQL doesn't fetch anything
        commands.forEach(c -> changeContext.addEntity(c, Entity.EMPTY));
        Set<EntityField<?, ?>> fieldsToFetch = currentStateConsumers
                .flatMap(consumer -> validateFieldsToFetch(flowConfig, changeOperation, consumer.getRequiredFields(commands, changeOperation), consumer))
                .collect(toSet());
        fetchEntities(commands, fieldsToFetch, changeOperation, changeContext, flowConfig);
    }

    private <E extends EntityType<E>> void fetchEntities(Collection<? extends ChangeEntityCommand<E>> commands, Set<EntityField<?, ?>> fieldsToFetch, ChangeOperation changeOperation, ChangeContext changeContext, ChangeFlowConfig<E> flowConfig) {
        if(changeOperation == CREATE) {
            entitiesToContextFetcher.fetchEntitiesByForeignKeys(commands, fieldsToFetch, changeContext, flowConfig);
        } else {
            entitiesToContextFetcher.fetchEntitiesByKeys(commands, fieldsToFetch, changeContext, flowConfig);
        }
    }

    private <E extends EntityType<E>, EF extends EntityField<?, ?>> Stream<EF> validateFieldsToFetch(ChangeFlowConfig<E> flowConfig, ChangeOperation changeOperation, Stream<EF> fieldsToFetch, CurrentStateConsumer<E> consumer) {
        if(changeOperation == CREATE) {
            return fieldsToFetch.filter(entityField -> {
                if (entityField.getDbAdapter().getTable().equals(flowConfig.getEntityType().getPrimaryTable())) {
                    throw new IllegalStateException("Field " + fieldsToFetch + " of the primary table is requested in CREATE flow by " + consumer);
                }
                return true;
            });
        } else {
            return fieldsToFetch;
        }
    }

    private <E extends EntityType<E>, T extends EntityChange<E>> Collection<T> filterCommands(Collection<T> changes, List<ChangesFilter<E>> changesFilters, ChangeOperation changeOperation, ChangeContext changeContext) {
        Collection<T> filteredChanges = ImmutableList.copyOf(changes);
        for (ChangesFilter<E> changesFilter : changesFilters) {
            filteredChanges = Lists.newArrayList(changesFilter.filter(filteredChanges, changeOperation, changeContext));
        }
        return filteredChanges;
    }

    private <E extends EntityType<E>> Collection<EntityChange<E>> validateChanges(Collection<? extends EntityChange<E>> changes, ChangesFilter<E> validationFilter, ChangeOperation changeOperation, ChangeContext changeContext) {
        return Lists.newArrayList(validationFilter.filter(changes, changeOperation, changeContext));
    }

    private <E extends EntityType<E>> void generateOutputRecursive(ChangeFlowConfig<E> flowConfig, Collection<? extends EntityChange<E>> commands, ChangeContext context) {
        for (OutputGenerator<E> outputGenerator : flowConfig.getOutputGenerators()) {
            Seq.of(DELETE, UPDATE, CREATE)
                    .map(op -> seq(commands).filter(cmd -> cmd.getChangeOperation() == op).toList())
                    .filter(list -> !list.isEmpty())
                    .forEach(list -> outputGenerator.generate(list, list.get(0).getChangeOperation(), context));
        }

        // invoke recursive
        flowConfig.childFlows().forEach(childFlow -> generateOutputChildFlowRecursive(commands, childFlow, context));
    }

    private <PARENT extends EntityType<PARENT>, CHILD extends EntityType<CHILD>> void generateOutputChildFlowRecursive(Collection<? extends EntityChange<PARENT>> entityChanges, ChangeFlowConfig<CHILD> childFlow, ChangeContext context) {
        generateOutputRecursive(childFlow, entityChanges.stream().flatMap(parent -> parent.getChildren(childFlow.getEntityType())).collect(toList()), context);
    }

    private <T> List<? extends T> only(Iterable<? extends T> items, Predicate<? super T> predicate) {
        return seq(items).filter(predicate).toList();
    }

    private Predicate<? super EntityChange<?>> withOperator(ChangeOperation op) {
        return cmd -> op == cmd.getChangeOperation();
    }

}
