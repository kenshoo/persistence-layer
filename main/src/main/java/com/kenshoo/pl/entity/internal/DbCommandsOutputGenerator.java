package com.kenshoo.pl.entity.internal;

import com.google.common.base.Stopwatch;
import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.data.*;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.spi.OutputGenerator;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jooq.*;
import org.jooq.lambda.Seq;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.kenshoo.pl.entity.ChangeOperation.CREATE;
import static com.kenshoo.pl.entity.HierarchyKeyPopulator.autoInc;
import static com.kenshoo.pl.entity.HierarchyKeyPopulator.fromContext;
import static org.jooq.lambda.Seq.seq;
import static org.jooq.lambda.Seq.toList;
import static org.jooq.lambda.function.Functions.not;


public class DbCommandsOutputGenerator<E extends EntityType<E>> implements OutputGenerator<E> {

    private final E entityType;
    private final CommandsExecutor commandsExecutor;
    private final SecondaryTableMandatoryFieldProvider secondaryTableMandatoryFieldProvider;
    private Map<DataTable, EntityField<E, ?>> secondaryTableMandatoryFieldMap;

    public DbCommandsOutputGenerator(E entityType, PLContext plContext) {
        this.entityType = entityType;
        this.commandsExecutor = CommandsExecutor.of(plContext.dslContext());
        this.secondaryTableMandatoryFieldProvider = new SecondaryTableMandatoryFieldProvider<>(entityType);
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

    private <T> void translateChange(final EntityChange<E> entityChange, final FieldChange<E, T> change, final EntityField<E, T> entityField, final ChangesContainer changesContainer, final ChangeOperation changeOperation, final ChangeContext changeContext) {
        final DataTable fieldTable = entityField.getDbAdapter().getTable();
        final DataTable primaryTable = entityType.getPrimaryTable();
        AbstractRecordCommand recordCommand;
        if (fieldTable == primaryTable) {
            if (changeOperation == CREATE) {
                recordCommand = changesContainer.getInsert(primaryTable, entityChange, () -> newCreateRecord(entityChange));
            } else {
                recordCommand = changesContainer.getUpdate(primaryTable, entityChange, () -> new UpdateRecordCommand(primaryTable, getDatabaseId(entityChange)));
            }
        } else {
            if (shouldCreateSecondaryEntity(changeContext.getEntity(entityChange), fieldTable)) {
                recordCommand = changesContainer.getInsert(fieldTable, entityChange, () -> {
                    final CreateRecordCommand createRecordCommand = new CreateRecordCommand(fieldTable);
                    populate(foreignKeyValues(entityChange, changeOperation, changeContext, fieldTable), createRecordCommand);
                    return createRecordCommand;
                });
            } else {
                recordCommand = changesContainer.getUpdate(fieldTable, entityChange, () -> new UpdateRecordCommand(fieldTable, foreignKeyValues(entityChange, changeOperation, changeContext, fieldTable)));
            }
        }
        populateFieldChange(change, recordCommand);
    }

    private boolean shouldCreateSecondaryEntity(CurrentEntityState entity, DataTable table) {
        return entity.safeGet(this.secondaryTableMandatoryFieldMap.get(table)).isAbsent();
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
        Stream<? extends EntityField<?, ?>> requiredFields = Stream.empty();
        if (fieldsToUpdate.stream().anyMatch(this::isSecondaryField)) {
            requiredFields = getMandatoryFields(fieldsToUpdate);
            // If update, find which secondary tables are affected.
            // For those secondary tables take their foreign keys to primary, translate referenced fields of primary to EntityFields and add them to fields to fetch
            if (changeOperation == ChangeOperation.UPDATE) {
                requiredFields = Stream.concat(requiredFields, getPrimaryKeyFields(entityType));
            }
        }

        return requiredFields;
    }

    private Stream<? extends EntityField<?, ?>> getMandatoryFields(Collection<? extends EntityField<E, ?>> fieldsToUpdate) {
        this.secondaryTableMandatoryFieldMap = fieldsToUpdate.stream()
                .filter(this::isSecondaryField)
                .map(field -> field.getDbAdapter().getTable())
                .distinct()
                .collect(Collectors.toMap(table -> table, (Function<DataTable, EntityField<E, ?>>) this.secondaryTableMandatoryFieldProvider::get));
        return this.secondaryTableMandatoryFieldMap.values().stream();
    }

    private boolean isSecondaryField(EntityField<E, ?> field) {
        return !field.getDbAdapter().getTable().equals(entityType.getPrimaryTable());
    }

    private Stream<EntityField<?, ?>> getPrimaryKeyFields(E entityType) {
        DataTable primaryTable = entityType.getPrimaryTable();
        List<TableField<Record, ?>> primaryKeyFields = primaryTable.getPrimaryKey().getFields();
        return entityType.getFields()
                .filter(entityField -> entityField.getDbAdapter().getTableFields().anyMatch(primaryKeyFields::contains))
                .map(entityField -> (EntityField<?, ?>) entityField);
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
