package com.kenshoo.pl.data;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.jooq.FieldAndValue;
import com.kenshoo.pl.data.CreateRecordCommand.OnDuplicateKey;
import org.jooq.*;
import org.jooq.impl.DSL;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.jooq.lambda.Seq.seq;

public class CommandsExecutor {

    final private DSLContext dslContext;

    public CommandsExecutor(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public static CommandsExecutor of(DSLContext dslContext) {
        return new CommandsExecutor(dslContext);
    }

    public AffectedRows executeInserts(final DataTable table, Collection<? extends CreateRecordCommand> commands) {
        return executeCommands(commands, homogeneousCommands -> executeInsertCommands(table, homogeneousCommands, OnDuplicateKey.FAIL));
    }

    public AffectedRows executeInsertsOnDuplicateKeyIgnore(final DataTable table, Collection<? extends CreateRecordCommand> commands) {
        return executeCommands(commands, homogeneousCommands -> executeInsertCommands(table, homogeneousCommands, OnDuplicateKey.IGNORE));
    }

    public AffectedRows executeInsertsOnDuplicateKeyUpdate(final DataTable table, Collection<? extends CreateRecordCommand> commands) {
        return executeCommands(commands, homogeneousCommands -> executeInsertCommands(table, homogeneousCommands, OnDuplicateKey.UPDATE));
    }

    public AffectedRows executeUpdates(final DataTable table, Collection<? extends UpdateRecordCommand> commands) {
        return executeCommands(commands, homogeneousCommands -> executeUpdateCommands(table, homogeneousCommands));
    }

    public AffectedRows executeDeletes(final DataTable table, Collection<? extends DeleteRecordCommand> commands) {
        if (commands.isEmpty()) {
            return AffectedRows.empty();
        }
        return executeDeleteCommands(table, commands);
    }

    private <C extends AbstractRecordCommand> AffectedRows executeCommands(Collection<? extends C> commands, HomogeneousChunkExecutor<C> homogeneousChunkExecutor) {
        AffectedRows updated = AffectedRows.empty();
        List<C> commandsLeft = new LinkedList<>(commands);
        while (!commandsLeft.isEmpty()) {
            C firstCommand = commandsLeft.remove(0);
            List<C> commandsToExecute = new ArrayList<>(Collections.singletonList(firstCommand));
            Set<String> firstCommandFields = getFieldsNames(firstCommand);
            Iterator<C> iterator = commandsLeft.iterator();
            while (iterator.hasNext()) {
                C command = iterator.next();
                if (getFieldsNames(command).equals(firstCommandFields)) {
                    commandsToExecute.add(command);
                    iterator.remove();
                }
            }
            updated = updated.plus(homogeneousChunkExecutor.execute(commandsToExecute));
        }
        return updated;
    }

    private AffectedRows executeDeleteCommands(DataTable table, Collection<? extends DeleteRecordCommand> commandsToExecute) {
        DeleteWhereStep<Record> delete = dslContext.delete(table);
        Iterator<? extends DeleteRecordCommand> commandIt = commandsToExecute.iterator();
        DeleteRecordCommand command = commandIt.next();
        Condition condition = DSL.trueCondition();
        TableField[] tableFields = command.getId().getTableFields();
        for (TableField id : tableFields) {
            //noinspection unchecked
            condition = condition.and(id.eq((Object) null));
        }
        for (FieldAndValue<?> partitionFieldAndValue : table.getVirtualPartition()) {
            //noinspection unchecked
            condition = condition.and(((Field) partitionFieldAndValue.getField()).eq((Object) null));
        }
        delete.where(condition);

        BatchBindStep batch = dslContext.batch(delete);
        while (command != null) {
            List<Object> values = Stream.concat(Stream.of(command.getId().getValues()),
                    table.getVirtualPartition().stream().map(FieldAndValue::getValue)).collect(toList());
            batch.bind(values.toArray());
            command = commandIt.hasNext() ? commandIt.next() : null;
        }
        return AffectedRows.deleted(IntStream.of(batch.execute()).sum());
    }

    private AffectedRows executeUpdateCommands(DataTable table, List<? extends UpdateRecordCommand> commandsToExecute) {
        UpdateSetFirstStep update1 = dslContext.update(table);
        UpdateSetMoreStep update = null;
        UpdateRecordCommand command1 = commandsToExecute.get(0);
        if (!command1.getFields().findFirst().isPresent()) {
            return AffectedRows.empty();
        }
        for (Field<?> field : seq(command1.getFields())) {
            if (update != null) {
                update = update.set(field, (Object) null);
            } else {
                update = update1.set(field, (Object) null);
            }
        }
        assert update != null;
        Condition condition = DSL.trueCondition();
        TableField[] tableFields = command1.getId().getTableFields();
        for (TableField id : tableFields) {
            //noinspection unchecked
            condition = condition.and(id.eq((Object) null));
        }
        for (FieldAndValue<?> partitionFieldAndValue : table.getVirtualPartition()) {
            //noinspection unchecked
            condition = condition.and(((Field) partitionFieldAndValue.getField()).eq((Object) null));
        }
        update.where(condition);

        BatchBindStep batch = dslContext.batch(update);
        for (UpdateRecordCommand command : commandsToExecute) {
            List<Object> values = Stream.of(command.getValues(command1.getFields()),
                    Stream.of(command.getId().getValues()),
                    table.getVirtualPartition().stream().map(FieldAndValue::getValue)).flatMap(s -> s).collect(toList());
            batch.bind(values.toArray());
        }
        int[] execute = batch.execute();
        return AffectedRows.updated(IntStream.of(execute).sum());
    }

    private AffectedRows executeInsertCommands(DataTable table, List<? extends CreateRecordCommand> commandsToExecute, OnDuplicateKey onDuplicateKey) {

        final Optional<GeneratedKeyRecorder> generatedKeyRecorder = Optional.ofNullable(table.getIdentity())
                .map(identity -> new GeneratedKeyRecorder(identity.getField(), commandsToExecute.size()));

        DSLContext dslContext = generatedKeyRecorder.map(g -> g.newRecordingJooq(this.dslContext)).orElse(this.dslContext);

        CreateRecordCommand firstCommand = commandsToExecute.get(0);
        Collection<Field<?>> fields = Stream.concat(firstCommand.getFields(), table.getVirtualPartition().stream().map(FieldAndValue::getField)).collect(toList());
        InsertValuesStepN<Record> insertValuesStepN = dslContext.insertInto(table, fields).values(new Object[fields.size()]);
        Insert insert = insertValuesStepN;
        switch (onDuplicateKey) {
            case IGNORE:
                insert = insertValuesStepN.onDuplicateKeyIgnore();
                break;
            case UPDATE:
                InsertOnDuplicateSetStep<Record> insertOnDuplicateSetStep = insertValuesStepN.onDuplicateKeyUpdate();
                for (Field<?> field : seq(firstCommand.getFields())) {
                    //noinspection unchecked
                    insertOnDuplicateSetStep = insertOnDuplicateSetStep.set((Field) field, (Object) null);
                }
                insert = (Insert) insertOnDuplicateSetStep;
                break;
        }

        BatchBindStep batch = dslContext.batch(insert);

        for (AbstractRecordCommand command : commandsToExecute) {
            List<Object> values = Stream.concat(command.getValues(firstCommand.getFields()), table.getVirtualPartition().stream().map(FieldAndValue::getValue)).collect(toList());
            if (onDuplicateKey == OnDuplicateKey.UPDATE) {
                values = Stream.concat(values.stream(), values.stream()).collect(toList());
            }
            batch.bind(values.toArray());
        }
        int[] result = batch.execute();
        // See https://dev.mysql.com/doc/refman/5.7/en/mysql-affected-rows.html for explanation
        // In case of regular INSERT (without IGNORE or ON DUPLICATE KEY UPDATE) the result is -2 for every inserted row
        int inserted = (int) IntStream.of(result).filter(i -> i == 1 || i == -2).count();
        int updated = (int) IntStream.of(result).filter(i -> i == 2).count();

        generatedKeyRecorder
                .map(GeneratedKeyRecorder::getGeneratedKeys)
                .ifPresent(generatedKeys -> setIdsToCommands(table.getIdentity().getField(), commandsToExecute, generatedKeys));

        return AffectedRows.insertedAndUpdated(inserted, updated);
    }

    private void setIdsToCommands(Field idField, List<? extends CreateRecordCommand> commandsToExecute, List<Object> generatedKeys) {
        seq(commandsToExecute).zip(generatedKeys).forEach(pair -> pair.v1.set(idField, pair.v2));
    }

    private Set<String> getFieldsNames(AbstractRecordCommand command) {
        return command.getFields().map(Field::getName).collect(toSet());
    }

    @FunctionalInterface
    interface HomogeneousChunkExecutor<C extends AbstractRecordCommand> {
        AffectedRows execute(List<C> commands);
    }

}

