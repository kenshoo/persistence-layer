package com.kenshoo.pl.entity;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.kenshoo.pl.entity.audit.AuditRecord;
import com.kenshoo.pl.entity.internal.*;
import com.kenshoo.pl.entity.internal.audit.RecursiveAuditRecordGenerator;
import com.kenshoo.pl.entity.internal.validators.ValidationFilter;
import com.kenshoo.pl.entity.spi.CurrentStateConsumer;
import com.kenshoo.pl.entity.spi.OutputGenerator;
import com.kenshoo.pl.entity.spi.ValidationException;
import org.jooq.DSLContext;
import org.jooq.lambda.Seq;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.kenshoo.pl.entity.ChangeOperation.*;
import static com.kenshoo.pl.entity.HierarchyKeyPopulator.*;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.jooq.lambda.Seq.seq;


public class PersistenceLayer<ROOT extends EntityType<ROOT>> {

    private final PLContext plContext;
    private final FieldsToFetchBuilder<ROOT> fieldsToFetchBuilder;
    private DeletionCommandPopulator deletionCommandPopulator;
    private final RecursiveAuditRecordGenerator recursiveAuditRecordGenerator;

    public PersistenceLayer(final DSLContext dslContext) {
        this(new PLContext.Builder(dslContext).build());
    }

    public PersistenceLayer(final PLContext plContext) {
        this.plContext = plContext;
        this.fieldsToFetchBuilder = new FieldsToFetchBuilder<>();
        this.deletionCommandPopulator = new DeletionCommandPopulator(plContext);
        this.recursiveAuditRecordGenerator = new RecursiveAuditRecordGenerator();
    }

    public <PK extends Identifier<ROOT>>
    CreateResult<ROOT, PK> create(Collection<? extends CreateEntityCommand<ROOT>> commands, ChangeFlowConfig<ROOT> flowConfig, UniqueKey<ROOT> primaryKey) {
        ChangeContext changeContext = makeChanges(commands, flowConfig);
        CreateResult<ROOT, PK> results = toCreateResults(commands, changeContext);
        setIdentifiersToSuccessfulCommands(flowConfig, primaryKey, changeContext, results);
        return results;
    }

    public CreateResult<ROOT, Identifier<ROOT>> create(Collection<? extends CreateEntityCommand<ROOT>> commands, ChangeFlowConfig<ROOT> flowConfig) {
        return create(commands, flowConfig, flowConfig.getEntityType().getPrimaryKey());
    }

    private <PK extends Identifier<ROOT>>
    void setIdentifiersToSuccessfulCommands(ChangeFlowConfig<ROOT> flowConfig, UniqueKey<ROOT> primaryKey, ChangeContext changeContext, CreateResult<ROOT, PK> results) {
        final Optional<EntityField<ROOT, Object>> optionalIdentityField = flowConfig.getPrimaryIdentityField();

        seq(results.iterator())
            .filter(EntityChangeResult::isSuccess)
            .map(EntityChangeResult::getCommand)
            .forEach(cmd -> {
                optionalIdentityField.ifPresent(idField -> populateIdentityField(cmd, changeContext, idField));
                cmd.setIdentifier(primaryKey.createIdentifier(cmd));
            });
    }

    private <PK extends Identifier<ROOT>>
    CreateResult<ROOT, PK> toCreateResults(Collection<? extends CreateEntityCommand<ROOT>> commands, ChangeContext changeContext) {
        return new CreateResult<>(
                    seq(commands).map(cmd -> new EntityCreateResult<>(cmd, changeContext.getValidationErrors(cmd))),
                    changeContext.getStats());
    }

    public <ID extends Identifier<ROOT>> UpdateResult<ROOT, ID> update(Collection<? extends UpdateEntityCommand<ROOT, ID>> commands, ChangeFlowConfig<ROOT> flowConfig) {
        ChangeContext changeContext = makeChanges(commands, flowConfig);
        return new UpdateResult<>(
                seq(commands).map(cmd -> new EntityUpdateResult<>(cmd, changeContext.getValidationErrors(cmd))),
                changeContext.getStats());
    }

    public <ID extends Identifier<ROOT>> DeleteResult<ROOT, ID> delete(Collection<? extends DeleteEntityCommand<ROOT, ID>> commands, ChangeFlowConfig<ROOT> flowConfig) {
        ChangeContext changeContext = makeChanges(commands, flowConfig);
        return new DeleteResult<>(
                seq(commands).map(cmd -> new EntityDeleteResult<>(cmd, changeContext.getValidationErrors(cmd))),
                changeContext.getStats());
    }

    public <ID extends Identifier<ROOT>> InsertOnDuplicateUpdateResult<ROOT, ID> upsert(Collection<? extends InsertOnDuplicateUpdateCommand<ROOT, ID>> commands, ChangeFlowConfig<ROOT> flowConfig) {
        ChangeContext changeContext = makeChanges(commands, flowConfig);
        InsertOnDuplicateUpdateResult<ROOT, ID> results = toUpsertResults(commands, changeContext);
        populateIdentityFieldToSuccessfulUpserts(flowConfig, changeContext, results);
        return results;
    }

    private <ID extends Identifier<ROOT>> InsertOnDuplicateUpdateResult<ROOT, ID> toUpsertResults(Collection<? extends InsertOnDuplicateUpdateCommand<ROOT, ID>> commands, ChangeContext changeContext) {
        return new InsertOnDuplicateUpdateResult<>(
                    seq(commands).map(cmd -> new EntityInsertOnDuplicateUpdateResult<>(cmd, changeContext.getValidationErrors(cmd))),
                    changeContext.getStats());
    }

    private <ID extends Identifier<ROOT>> void populateIdentityFieldToSuccessfulUpserts(ChangeFlowConfig<ROOT> flowConfig, ChangeContext changeContext, InsertOnDuplicateUpdateResult<ROOT, ID> results) {
        flowConfig.getPrimaryIdentityField().ifPresent(identityField -> seq(results.iterator())
                .filter(EntityChangeResult::isSuccess)
                .map(EntityChangeResult::getCommand)
                .filter(cmd -> cmd.getChangeOperation() == CREATE)
                .forEach(cmd -> populateIdentityField(cmd, changeContext, identityField)));
    }

    private ChangeContext makeChanges(Collection<? extends ChangeEntityCommand<ROOT>> commands, ChangeFlowConfig<ROOT> flowConfig) {
        deletionCommandPopulator.handleRecursive(commands, flowConfig);
        ChangeContextImpl context = new ChangeContextImpl(Hierarchy.build(flowConfig), flowConfig.getFeatures());
        context.addFetchRequests(fieldsToFetchBuilder.build(commands, flowConfig));
        prepareRecursive(commands, context, flowConfig);
        Collection<? extends ChangeEntityCommand<ROOT>> validCmds = seq(commands).filter(cmd -> !context.containsError(cmd)).toList();
        ChangeContext overridingCtx = new OverridingContext(context);
        if (!validCmds.isEmpty()) {
            flowConfig.retryer().run((() -> dslContext().transaction((configuration) -> generateOutputRecursive(flowConfig, validCmds, overridingCtx))));
        }
        final Stream<? extends AuditRecord<ROOT>> auditRecords =
            recursiveAuditRecordGenerator.generateMany(flowConfig,
                                                       validCmds.stream(),
                                                       overridingCtx);
        plContext.auditRecordPublisher().publish(auditRecords);
        return overridingCtx;
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

        populateParentKeysIntoChildren(context, validChanges);

        flow.childFlows().forEach(childFlow -> prepareChildFlowRecursive(validChanges, childFlow, context));
    }

    private <E extends EntityType<E>> void populateParentKeysIntoChildren(ChangeContext context, Collection<? extends ChangeEntityCommand<E>> commands) {
        new Builder<E>()
                .with(context.getHierarchy())
                .whereParentFieldsAre(notAutoInc())
                .gettingValues(fromCommands()).build()
                .populateKeysToChildren(only(commands, withOperator(CREATE)));

        new Builder<E>()
                .with(context.getHierarchy())
                .whereParentFieldsAre(anyField())
                .gettingValues(fromContext(context)).build()
                .populateKeysToChildren(only(commands, withOperator(UPDATE)));
    }

    private <PARENT extends EntityType<PARENT>, CHILD extends EntityType<CHILD>> void prepareChildFlowRecursive(List<? extends ChangeEntityCommand<PARENT>> validChanges, ChangeFlowConfig<CHILD> childFlow, ChangeContext context) {
        prepareRecursive(validChanges.stream().flatMap(parent -> parent.getChildren(childFlow.getEntityType())).collect(toList()), context, childFlow);
    }

    private boolean isMissing(ChangeEntityCommand<?> cmd, ChangeContext context) {
        return context.getEntity(cmd) == CurrentEntityState.EMPTY;
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
        fetcher(flowConfig.getFeatures()).fetchEntities(commands, changeOperation, changeContext, flowConfig);
        changeContext.getStats().addFetchTime(stopwatch.elapsed(TimeUnit.MILLISECONDS));

        commands = filterCommands(commands, getSupportedFilters(flowConfig.getPostFetchFilters(), changeOperation), changeOperation, changeContext);
        commands = resolveSuppliersAndFilterErrors(commands, changeContext);
        commands = filterCommands(commands, getSupportedFilters(flowConfig.getPostSupplyFilters(), changeOperation), changeOperation, changeContext);
        enrichCommandsPostFetch(commands, flowConfig, changeOperation, changeContext);

        return validateChanges(commands, new ValidationFilter<>(flowConfig.getValidators()), changeOperation, changeContext);
    }

    private <E extends EntityType<E>> EntitiesToContextFetcher fetcher(FeatureSet features) {
        return new EntitiesToContextFetcher(new EntitiesFetcher(dslContext(), features));
    }

    private <E extends EntityType<E>, C extends ChangeEntityCommand<E>> Collection<C> resolveSuppliersAndFilterErrors(Collection<C> commands, ChangeContext changeContext) {
        List<C> validCommands = Lists.newArrayListWithCapacity(commands.size());

        for (C command : commands) {
            CurrentEntityState currentState = changeContext.getEntity(command);
            try {
                command.resolveSuppliers(currentState);
                validCommands.add(command);
            } catch (ValidationException e) {
                changeContext.addValidationError(command, e.getValidationError());
            }
        }

        return validCommands;
    }

    private <E extends EntityType<E>, C extends ChangeEntityCommand<E>> Collection<C> filterCommands(Collection<C> commands, ChangeFlowConfig<E> flowConfig, ChangeOperation changeOperation, ChangeContext changeContext) {
        if(changeOperation == CREATE) {
            return Lists.newArrayList(new RequiredFieldsChangesFilter<>(flowConfig.getRequiredRelationFields()).filter(commands, changeOperation, changeContext));
        } else {
            return commands;
        }
    }

    private <E extends EntityType<E>> void enrichCommandsPostFetch(Collection<? extends ChangeEntityCommand<E>> commands, ChangeFlowConfig<E> flowConfig, ChangeOperation changeOperation, ChangeContext changeContext) {
        flowConfig.getPostFetchCommandEnrichers().stream()
                .filter(CurrentStateConsumer.supporting(changeOperation))
                .forEach(enricher -> enricher.enrich(commands,  changeOperation, changeContext));
    }


    private <E extends EntityType<E>> List<ChangesFilter<E>> getSupportedFilters(List<ChangesFilter<E>> filters, ChangeOperation changeOperation) {
        return filters.stream().filter(CurrentStateConsumer.supporting(changeOperation)).collect(toList());
    }

    private <E extends EntityType<E>, T extends ChangeEntityCommand<E>> Collection<T> filterCommands(Collection<T> changes, List<ChangesFilter<E>> changesFilters, ChangeOperation changeOperation, ChangeContext changeContext) {
        Collection<T> filteredChanges = ImmutableList.copyOf(changes);
        for (ChangesFilter<E> changesFilter : changesFilters) {
            filteredChanges = Lists.newArrayList(changesFilter.filter(filteredChanges, changeOperation, changeContext));
        }
        return filteredChanges;
    }

    private <E extends EntityType<E>> Collection<EntityChange<E>> validateChanges(Collection<? extends ChangeEntityCommand<E>> changes, ChangesFilter<E> validationFilter, ChangeOperation changeOperation, ChangeContext changeContext) {
        return Lists.newArrayList(validationFilter.filter(changes, changeOperation, changeContext));
    }

    private <E extends EntityType<E>> void generateOutputRecursive(ChangeFlowConfig<E> flowConfig, Collection<? extends ChangeEntityCommand<E>> commands, ChangeContext context) {
        for (OutputGenerator<E> outputGenerator : flowConfig.getOutputGenerators()) {
            Seq.of(DELETE, UPDATE, CREATE)
                    .filter(CurrentStateConsumer.supporting(outputGenerator))
                    .map(op -> seq(commands).filter(cmd -> cmd.getChangeOperation() == op).toList())
                    .filter(list -> !list.isEmpty())
                    .forEach(list -> outputGenerator.generate(list, list.get(0).getChangeOperation(), context));
        }

        // invoke recursive
        flowConfig.childFlows().forEach(childFlow -> generateOutputChildFlowRecursive(commands, childFlow, context));
    }

    private <PARENT extends EntityType<PARENT>, CHILD extends EntityType<CHILD>> void generateOutputChildFlowRecursive(Collection<? extends ChangeEntityCommand<PARENT>> entityChanges, ChangeFlowConfig<CHILD> childFlow, ChangeContext context) {
        generateOutputRecursive(childFlow, entityChanges.stream().flatMap(parent -> parent.getChildren(childFlow.getEntityType())).collect(toList()), context);
    }

    private <T> List<? extends T> only(Iterable<? extends T> items, Predicate<? super T> predicate) {
        return seq(items).filter(predicate).toList();
    }

    private Predicate<? super EntityChange<?>> withOperator(ChangeOperation op) {
        return cmd -> op == cmd.getChangeOperation();
    }

    private void populateIdentityField(final ChangeEntityCommand<ROOT> cmd, final ChangeContext changeContext, final EntityField<ROOT, Object> idField) {
        final CurrentEntityState currentState = Optional.ofNullable(changeContext.getEntity(cmd))
                                      .orElseThrow(() -> new IllegalStateException("Could not find entity of command in the change context"));
        cmd.set(idField,  currentState.get(idField));
    }

    private DSLContext dslContext() {
        return plContext.dslContext();
    }
}
