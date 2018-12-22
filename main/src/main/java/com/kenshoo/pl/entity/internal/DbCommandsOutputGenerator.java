package com.kenshoo.pl.entity.internal;

import com.google.common.base.Stopwatch;
import com.kenshoo.pl.jooq.DataTable;
import com.kenshoo.pl.data.*;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.spi.OutputGenerator;
import org.jooq.ForeignKey;
import org.jooq.Record;
import org.jooq.TableField;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;


public class DbCommandsOutputGenerator<E extends EntityType<E>> implements OutputGenerator<E> {

    private final E entityType;
    private final CommandsExecutor commandsExecutor;

    public DbCommandsOutputGenerator(E entityType, CommandsExecutor commandsExecutor) {
        this.commandsExecutor = commandsExecutor;
        this.entityType = entityType;
    }

    @Override
    public void generate(Collection<? extends EntityChange<E>> entityChanges, ChangeOperation changeOperation, ChangeContext changeContext) {
        ChangesContainer changesContainer = new ChangesContainer(entityType.onDuplicateKey());
        for (EntityChange<E> entityChange : entityChanges) {
            if (changeOperation == ChangeOperation.DELETE) {
                changesContainer.getDelete(entityType.getPrimaryTable(), entityChange, () -> new DeleteRecordCommand(entityType.getPrimaryTable(), getDatabaseId(entityChange)));
            } else {
                entityChange.getChanges().forEach(fieldChange -> translateChange(entityChange, fieldChange, changesContainer, changeOperation, changeContext));
            }
        }
        Stopwatch stopwatch = Stopwatch.createStarted();
        changesContainer.commit(commandsExecutor, changeContext.getStats());
        changeContext.getStats().addUpdateTime(stopwatch.elapsed(TimeUnit.MILLISECONDS));
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
            if (changeOperation == ChangeOperation.CREATE) {
                recordCommand = changesContainer.getInsert(primaryTable, entityChange, () -> newCreateRecord(entityChange));
            } else {
                recordCommand = changesContainer.getUpdate(primaryTable, entityChange, () -> new UpdateRecordCommand(primaryTable, getDatabaseId(entityChange)));
            }
        } else {
            recordCommand = changesContainer.getInsertOnDuplicateUpdate(fieldTable, entityChange, () -> {
                CreateRecordCommand createRecordCommand = new CreateRecordCommand(fieldTable);
                populate(foreignKeyValues(entityChange, changeOperation, changeContext, fieldTable), createRecordCommand);
                return createRecordCommand;
            });

        }
        populateFieldChange(change, recordCommand);
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
    public Stream<EntityField<?, ?>> getRequiredFields(Collection<? extends ChangeEntityCommand<E>> changeEntityCommands, ChangeOperation changeOperation) {
        // If update, find which secondary tables are affected.
        // For those secondary tables take their foreign keys to primary, translate referenced fields of primary to EntityFields and add them to fields to fetch
        if (changeOperation == ChangeOperation.UPDATE) {
            DataTable primaryTable = entityType.getPrimaryTable();
            if (isChangesInSecondaryTables(changeEntityCommands, primaryTable)) {
                return getPrimaryKeyFields(entityType);
            }
        }

        return Stream.empty();
    }

    private boolean isChangesInSecondaryTables(Collection<? extends ChangeEntityCommand<E>> changeEntityCommands, DataTable primaryTable) {
        return changeEntityCommands.stream()
                .flatMap(ChangeEntityCommand::getChangedFields)
                .anyMatch(field -> field.getDbAdapter().getTable() != primaryTable);
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
        ForeignKey<Record, Record> foreignKey = childTable.getForeignKey(((ChangeEntityCommand) cmd).getEntityType().getPrimaryTable());
        Collection<EntityField<E, ?>> parentFields = entityType(cmd).findFields(foreignKey.getKey().getFields());
        Object[] values = changeOperation == ChangeOperation.CREATE ? EntityDbUtil.getFieldValues(parentFields, cmd) : EntityDbUtil.getFieldValues(parentFields, context.getEntity(cmd));
        if (foreignKey.getFields().size() != values.length) {
            throw new IllegalStateException("Foreign key from " + childTable.getName() + " doesn't have the same number of fields as " + foreignKey);
        }
        return new DatabaseId(
                foreignKey.getFields().toArray(new TableField<?, ?>[foreignKey.getFields().size()]),
                values);
    }

    private EntityType<E> entityType(EntityChange<E> cmd) {
        return ((ChangeEntityCommand)cmd).getEntityType();
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
        if(keysToParent != null) {
            databaseId = databaseId.append(EntityDbUtil.getDatabaseId(entityChange.getKeysToParent()));
        }
        return databaseId;
    }

}
