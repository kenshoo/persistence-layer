package com.kenshoo.pl.entity.internal;

import com.google.common.base.Stopwatch;
import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.data.*;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.spi.OutputGenerator;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jooq.ForeignKey;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.lambda.Seq;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.kenshoo.pl.entity.ChangeOperation.CREATE;
import static com.kenshoo.pl.entity.ChangeOperation.UPDATE;
import static com.kenshoo.pl.entity.HierarchyKeyPopulator.autoInc;
import static com.kenshoo.pl.entity.HierarchyKeyPopulator.fromContext;
import static com.kenshoo.pl.entity.internal.SecondaryTableRelationExtractor.relationUsingTableFieldsOfPrimary;
import static com.kenshoo.pl.entity.internal.SecondaryTableRelationExtractor.relationUsingTableFieldsOfSecondary;
import static java.util.stream.Collectors.toList;
import static org.jooq.lambda.Seq.seq;
import static org.jooq.lambda.function.Functions.not;


public class DbCommandsOutputGenerator<E extends EntityType<E>> implements OutputGenerator<E> {

    private final E entityType;
    private final CommandsExecutor commandsExecutor;

    public DbCommandsOutputGenerator(E entityType, PLContext plContext) {
        this.entityType = entityType;
        this.commandsExecutor = CommandsExecutor.of(plContext.dslContext());
    }

    @Override
    public void generate(Collection<? extends EntityChange<E>> entityChanges, ChangeOperation operator, ChangeContext changeContext) {
        Stopwatch stopwatch = Stopwatch.createStarted();

        if (operator == ChangeOperation.DELETE) {
            generateForDelete(changeContext, entityChanges);
        } else {
            final ChangesContainer primaryTableCommands =
                    generateForCreateOrUpdate(entityChanges,
                            this::isOfPrimaryTable,
                            operator,
                            changeContext);

            entityType.getPrimaryIdentityField().ifPresent(identityField -> {
                        if (operator == CREATE) {
                            populateGeneratedIdsToContext(identityField,
                                    entityChanges,
                                    changeContext,
                                    primaryTableCommands);

                            //noinspection unchecked
                            new HierarchyKeyPopulator.Builder<E>()
                                    .with(changeContext.getHierarchy())
                                    .whereParentFieldsAre(autoInc())
                                    .gettingValues(fromContext(changeContext)).build()
                                    .populateKeysToChildren((Collection<? extends ChangeEntityCommand<E>>) entityChanges);
                        }
                    }
            );

            generateForCreateOrUpdate(entityChanges,
                    not(this::isOfPrimaryTable),
                    operator,
                    changeContext);
        }

        changeContext.getStats().addUpdateTime(stopwatch.elapsed(TimeUnit.MILLISECONDS));
    }

    private void populateGeneratedIdsToContext(final EntityField<E, Object> identityField,
                                               Collection<? extends EntityChange<E>> entityChanges,
                                               ChangeContext changeContext,
                                               ChangesContainer changesContainer) {
        final TableField<Record, ?> identityTableField = getFirstTableField(identityField);

        seq(entityChanges)
                .map(change -> ImmutablePair.of(change, changesContainer.getInsert(entityType.getPrimaryTable(), change)))
                .filter(pair -> pair.getRight().isPresent())
                .forEach(pair -> {
                    final CreateRecordCommand cmd = pair.getRight().get();
                    Object generatedValue = cmd.get(identityTableField);
                    changeContext.addEntity(pair.getLeft(), new EntityWithGeneratedId(identityField, generatedValue));
                });
    }

    private TableField<Record, ?> getFirstTableField(final EntityField<E, Object> entityField) {
        return entityField.getDbAdapter().getTableFields()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No table fields found for an entity field"));
    }

    private boolean isOfPrimaryTable(FieldChange<E, ?> f) {
        return f.getField().getDbAdapter().getTable() == entityType.getPrimaryTable();
    }

    private <T> void translateChange(EntityChange<E> entityChange, FieldChange<E, T> change, ChangesContainer changesContainer, ChangeOperation changeOperation, ChangeContext changeContext) {
        EntityField<E, T> entityField = change.getField();
        if (!entityField.isVirtual()) {
            translateChange(entityChange, change, entityField, changesContainer, changeOperation, changeContext);
        }
    }

    private <T> void translateChange(final EntityChange<E> entityChange, final FieldChange<E, T> change, final EntityField<E, T> entityField, final ChangesContainer changesContainer, final ChangeOperation operator, final ChangeContext ctx) {
        final DataTable fieldTable = entityField.getDbAdapter().getTable();
        final DataTable primaryTable = entityType.getPrimaryTable();
        AbstractRecordCommand recordCommand;
        if (fieldTable == primaryTable) {
            if (operator == CREATE) {
                recordCommand = changesContainer.getInsert(primaryTable, entityChange, () -> newCreateRecord(entityChange));
            } else {
                recordCommand = changesContainer.getUpdate(primaryTable, entityChange, () -> new UpdateRecordCommand(primaryTable, getDatabaseId(entityChange)));
            }
        } else {
            var foreignKeyValues = foreignKeyValues(entityChange, operator, ctx, fieldTable);
            if (operator == CREATE || rowNotYetExistsInSecondary(fieldTable, ctx.getEntity(entityChange), ctx, foreignKeyValues)) {
                recordCommand = changesContainer.getInsert(fieldTable, entityChange, () -> {
                    var createRecordCommand = new CreateRecordCommand(fieldTable);
                    populate(foreignKeyValues, createRecordCommand);
                    return createRecordCommand;
                });
            } else {
                recordCommand = changesContainer.getUpdate(fieldTable, entityChange, () -> new UpdateRecordCommand(fieldTable, foreignKeyValues));
            }
        }
        populateFieldChange(change, recordCommand);
    }

    private boolean rowNotYetExistsInSecondary(DataTable table, CurrentEntityState fetchedFields, ChangeContext ctx, DatabaseId fkToPrimary) {
        var keyFieldFromSecondaryToPrimary = seq(ctx.getFetchRequests())
                .map(FieldFetchRequest::getEntityField)
                .filter(field -> Seq.of(fkToPrimary.getTableFields()).contains(field.getDbAdapter().getFirstTableField()))
                .findFirst(field -> field.getDbAdapter().getTable() == table).get();
        return fetchedFields.safeGet(keyFieldFromSecondaryToPrimary).isNullOrAbsent();
    }

    private CreateRecordCommand newCreateRecord(EntityChange<E> entityChange) {
        CreateRecordCommand cmd = new CreateRecordCommand(entityType.getPrimaryTable());
        populateParentKeys(entityChange, cmd);
        return cmd;
    }

    private <T> void populateFieldChange(FieldChange<?, T> change, AbstractRecordCommand recordCommand) {
        T value = change.getValue();
        Iterator<TableField<Record, ?>> tableFields = change.getField().getDbAdapter().getTableFields().iterator();
        Iterator<Object> values = change.getField().getDbAdapter().getDbValues(value).iterator();
        while (tableFields.hasNext()) {
            //noinspection unchecked
            recordCommand.set((TableField<Record, Object>) tableFields.next(), values.next());
        }
    }

    @Override
    public Stream<? extends EntityField<?, ?>> requiredFields(Collection<? extends EntityField<E, ?>> fieldsToUpdate, ChangeOperation changeOperation) {
        if (changeOperation != UPDATE) {
            return Stream.empty();
        }

        var secondaryTables = secondaryTables(fieldsToUpdate).collect(toList());

        if (secondaryTables.isEmpty()) {
            return Stream.empty();
        }

        var secondaryTableFieldsThatCannotBeNull = secondaryTables.stream()
                .map(table -> relationUsingTableFieldsOfSecondary(table, entityType).findFirst().get());

        Stream<EntityField<E, ?>> requiredPrimaryTableIds = secondaryTables.stream()
                .flatMap(table -> relationUsingTableFieldsOfPrimary(table, entityType));

        return Stream.concat(secondaryTableFieldsThatCannotBeNull, requiredPrimaryTableIds);
    }

    private Stream<DataTable> secondaryTables(Collection<? extends EntityField<E, ?>> fieldsToUpdate) {
        return fieldsToUpdate.stream()
                .filter(this::isSecondaryField)
                .map(field -> field.getDbAdapter().getTable())
                .distinct();
    }

    private boolean isSecondaryField(EntityField<E, ?> field) {
        return field.getEntityType() == entityType && !entityType.getPrimaryTable().equals(field.getDbAdapter().getTable());
    }

    private void populate(DatabaseId id, AbstractRecordCommand toRecord) {
        for (int i = 0; i < id.getTableFields().length; i++) {
            //noinspection unchecked
            TableField<Record, Object> field = (TableField<Record, Object>) id.getTableFields()[i];
            toRecord.set(field, id.getValues()[i]);
        }
    }

    private DatabaseId foreignKeyValues(EntityChange<E> cmd, ChangeOperation changeOperation, ChangeContext context, DataTable childTable) {
        ForeignKey<Record, Record> foreignKey = childTable.getForeignKey(entityType.getPrimaryTable());
        Collection<EntityField<E, ?>> parentFields = entityType(cmd).findFields(foreignKey.getKey().getFields());
        boolean hasIdentity = entityType.getPrimaryIdentityField().isPresent();
        Object[] values = changeOperation == CREATE && !hasIdentity ? EntityDbUtil.getFieldValues(parentFields, cmd) : EntityDbUtil.getFieldValues(parentFields, context.getEntity(cmd));
        if (foreignKey.getFields().size() != values.length) {
            throw new IllegalStateException("Foreign key from " + childTable.getName() + " doesn't have the same number of fields as " + foreignKey);
        }
        return new DatabaseId(
                foreignKey.getFields().toArray(new TableField<?, ?>[foreignKey.getFields().size()]),
                values);
    }

    private EntityType<E> entityType(EntityChange<E> cmd) {
        return ((ChangeEntityCommand) cmd).getEntityType();
    }

    private void populateParentKeys(EntityChange<E> entityChange, AbstractRecordCommand recordCommand) {
        if (entityChange.getKeysToParent() != null) {
            DatabaseId parentIdValues = EntityDbUtil.getDatabaseId(entityChange.getKeysToParent());
            populate(parentIdValues, recordCommand);
        }
    }

    private DatabaseId getDatabaseId(EntityChange<E> entityChange) {
        DatabaseId databaseId = EntityDbUtil.getDatabaseId(entityChange.getIdentifier());
        Identifier<E> keysToParent = entityChange.getKeysToParent();
        if (keysToParent != null) {
            databaseId = databaseId.append(EntityDbUtil.getDatabaseId(entityChange.getKeysToParent()));
        }
        return databaseId;
    }

    private void generateForDelete(final ChangeContext changeContext,
                                   final Iterable<? extends EntityChange<E>> entityChanges) {
        ChangesContainer changesContainer = new ChangesContainer(entityType.onDuplicateKey());
        entityChanges.forEach(entityChange ->
                changesContainer.getDelete(entityType.getPrimaryTable(),
                        entityChange,
                        () -> new DeleteRecordCommand(entityType.getPrimaryTable(),
                                getDatabaseId(entityChange))));
        changesContainer.commit(commandsExecutor, changeContext.getStats());
    }

    private ChangesContainer generateForCreateOrUpdate(final Iterable<? extends EntityChange<E>> entityChanges,
                                                       final Predicate<FieldChange<E, ?>> filter,
                                                       final ChangeOperation operator,
                                                       final ChangeContext changeContext) {

        final ChangesContainer tableCommands = new ChangesContainer(entityType.onDuplicateKey());

        seq(entityChanges).forEach(cmd ->
                cmd.getChanges()
                        .filter(filter)
                        .forEach(fieldChange -> translateChange(cmd,
                                fieldChange,
                                tableCommands,
                                operator,
                                changeContext)));

        tableCommands.commit(commandsExecutor, changeContext.getStats());
        return tableCommands;
    }
}
