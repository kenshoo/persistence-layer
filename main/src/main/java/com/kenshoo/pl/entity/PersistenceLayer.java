package com.kenshoo.pl.entity;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.kenshoo.pl.entity.internal.ChangesFilter;
import com.kenshoo.pl.entity.internal.EntitiesToContextFetcher;
import com.kenshoo.pl.entity.internal.EntityDbUtil;
import com.kenshoo.pl.entity.internal.RequiredFieldsCommandsFilter;
import com.kenshoo.pl.entity.internal.validators.ValidationFilter;
import com.kenshoo.pl.entity.spi.OutputGenerator;
import com.kenshoo.pl.entity.spi.ValidationException;
import org.jooq.DSLContext;
import org.jooq.lambda.Seq;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static com.kenshoo.pl.entity.ChangeOperation.CREATE;
import static com.kenshoo.pl.entity.ChangeOperation.DELETE;
import static com.kenshoo.pl.entity.ChangeOperation.UPDATE;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.jooq.lambda.Seq.seq;


public class PersistenceLayer<ROOT extends EntityType<ROOT>, PK extends Identifier<ROOT>> {

    private final DSLContext dslContext;
    private final EntitiesToContextFetcher entitiesToContextFetcher;
    private final FieldsToFetchBuilder<ROOT> fieldsToFetchBuilder;

    public PersistenceLayer(DSLContext dslContext) {
        this.dslContext = dslContext;
        this.entitiesToContextFetcher = new EntitiesToContextFetcher(dslContext);
        this.fieldsToFetchBuilder = new FieldsToFetchBuilder<>();
    }

    public CreateResult<ROOT, PK> create(Collection<? extends CreateEntityCommand<ROOT>> commands, ChangeFlowConfig<ROOT> flowConfig, UniqueKey<ROOT> primaryKey) {
        ChangeContext changeContext = new ChangeContext();
        makeChanges(commands, changeContext, flowConfig);
        CreateResult<ROOT, PK> results = new CreateResult<>(
                seq(commands).map(cmd -> new EntityCreateResult<ROOT, PK>(cmd, changeContext.getValidationErrors(cmd))),
                changeContext.getStats());


        Optional.ofNullable(flowConfig.getEntityType().getPrimaryTable().getIdentity()).ifPresent(identity -> {

            EntityField<ROOT, Object> idField = (EntityField<ROOT, Object>) flowConfig.getEntityType().findField(identity.getField()).get();

            seq(results.iterator())
                    .filter(r -> r.isSuccess())
                    .forEach(res -> res.getCommand().set(idField, changeContext.getEntity(res.getCommand()).get(idField)));
        });

        seq(results.iterator())
                .filter(r -> r.isSuccess())
                .forEach(res -> res.getCommand().setIdentifier(primaryKey.createValue(res.getCommand())));

        return results;
    }

    public <ID extends Identifier<ROOT>> UpdateResult<ROOT, ID> update(Collection<? extends UpdateEntityCommand<ROOT, ID>> commands, ChangeFlowConfig<ROOT> flowConfig) {
        ChangeContext changeContext = new ChangeContext();
        makeChanges(commands, changeContext, flowConfig);
        return new UpdateResult<>(
                seq(commands).map(cmd -> new EntityUpdateResult<>(cmd, changeContext.getValidationErrors(cmd))),
                changeContext.getStats());
    }

    public <ID extends Identifier<ROOT>> DeleteResult<ROOT, ID> delete(Collection<? extends DeleteEntityCommand<ROOT, ID>> commands, ChangeFlowConfig<ROOT> flowConfig) {
        ChangeContext changeContext = new ChangeContext();
        makeChanges(commands, changeContext, flowConfig);
        return new DeleteResult<>(
                seq(commands).map(cmd -> new EntityDeleteResult<>(cmd, changeContext.getValidationErrors(cmd))),
                changeContext.getStats());
    }

    public <ID extends Identifier<ROOT>> InsertOnDuplicateUpdateResult<ROOT, ID> upsert(Collection<? extends InsertOnDuplicateUpdateCommand<ROOT, ID>> commands, ChangeFlowConfig<ROOT> flowConfig) {
        ChangeContext changeContext = new ChangeContext();
        makeChanges(commands, changeContext, flowConfig);
        return new InsertOnDuplicateUpdateResult<>(
                seq(commands).map(cmd -> new EntityInsertOnDuplicateUpdateResult<>(cmd, changeContext.getValidationErrors(cmd))),
                changeContext.getStats());
    }

    private void makeChanges(Collection<? extends ChangeEntityCommand<ROOT>> commands, ChangeContext context, ChangeFlowConfig<ROOT> flowConfig) {
        context.addFetchRequests(fieldsToFetchBuilder.build(commands, flowConfig));
        prepareRecursive(commands, context, flowConfig);
        Collection<? extends ChangeEntityCommand<ROOT>> validCmds = seq(commands).filter(cmd -> !context.containsError(cmd)).toList();
        if (!validCmds.isEmpty()) {
            flowConfig.retryer().run((() -> dslContext.transaction((configuration) -> generateOutputRecursive(flowConfig, validCmds, context))));
        }
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

        List<? extends ChangeEntityCommand<E>> validChanges = seq(commands).filter(cmd -> !context.containsErrorNonRecursive(cmd)).toList();

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

        seq(parents).filter(p -> hasAnyChild(childFlow, p)).forEach(parent -> {
            final Object[] values = parent.getChangeOperation() == CREATE ? EntityDbUtil.getFieldValues(childToParent.to, parent) : EntityDbUtil.getFieldValues(childToParent.to, context.getEntity(parent));
            if (childToParent.to.size() != values.length) {
                throw new IllegalStateException("Found " + values.length + " values of " + childToParent.to.size() + " fields for foreign key from table " + childType.getPrimaryTable().getName());
            }
            final UniqueKeyValue<CHILD> keysToParent = new UniqueKeyValue<>(new UniqueKey<>(array(childToParent.from)), values);
            parent.getChildren(childType).forEach(child -> child.setKeysToParent(keysToParent));
        });
    }

    private <PARENT extends EntityType<PARENT>, CHILD extends EntityType<CHILD>> boolean hasAnyChild(ChangeFlowConfig<CHILD> childFlow, ChangeEntityCommand<PARENT> p) {
        return p.getChildren(childFlow.getEntityType()).findAny().isPresent();
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

        Stopwatch stopwatch = Stopwatch.createStarted();
        entitiesToContextFetcher.fetchEntities(commands, changeOperation, changeContext, flowConfig);
        changeContext.getStats().addFetchTime(stopwatch.elapsed(TimeUnit.MILLISECONDS));

        commands = filterCommands(commands, getSupportedFilters(flowConfig.getPostFetchFilters(), changeOperation), changeOperation, changeContext);
        commands = resolveSuppliersAndFilterErrors(commands, changeContext);
        commands = filterCommands(commands, getSupportedFilters(flowConfig.getPostSupplyFilters(), changeOperation), changeOperation, changeContext);
        enrichCommandsPostFetch(commands, flowConfig, changeOperation, changeContext);

        return validateChanges(commands, new ValidationFilter<>(flowConfig.getValidators()), changeOperation, changeContext);
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
