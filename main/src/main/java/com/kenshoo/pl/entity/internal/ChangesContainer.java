package com.kenshoo.pl.entity.internal;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.data.AbstractRecordCommand;
import com.kenshoo.pl.data.AffectedRows;
import com.kenshoo.pl.data.CommandsExecutor;
import com.kenshoo.pl.data.CreateRecordCommand;
import com.kenshoo.pl.data.DeleteRecordCommand;
import com.kenshoo.pl.data.UpdateRecordCommand;
import com.kenshoo.pl.entity.EntityChange;
import com.kenshoo.pl.entity.PersistentLayerStats;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;


public class ChangesContainer {

    private final CreateRecordCommand.OnDuplicateKey onDuplicateKey;
    private final Map<DataTable, IdToCommandMap<DeleteRecordCommand>> deletes = new HashMap<>();
    private final Map<DataTable, IdToCommandMap<UpdateRecordCommand>> updates = new HashMap<>();
    private final Map<DataTable, IdToCommandMap<CreateRecordCommand>> inserts = new HashMap<>();
    private final Map<DataTable, IdToCommandMap<CreateRecordCommand>> insertsOnDuplicateUpdate = new HashMap<>();

    public ChangesContainer(CreateRecordCommand.OnDuplicateKey onDuplicateKey) {
        this.onDuplicateKey = onDuplicateKey;
    }

    public AbstractRecordCommand getDelete(DataTable table, EntityChange entityChange, Supplier<DeleteRecordCommand> commandCreator) {
        //noinspection RedundantTypeArguments IntelliJ fails to compile without specification
        return getOrCreate(deletes, IdToCommandMap< DeleteRecordCommand>::new, commandCreator, table, entityChange);
    }

    public AbstractRecordCommand getUpdate(DataTable table, EntityChange entityChange, Supplier<UpdateRecordCommand> commandCreator) {
        //noinspection RedundantTypeArguments IntelliJ fails to compile without specification
        return getOrCreate(updates, IdToCommandMap< UpdateRecordCommand>::new, commandCreator, table, entityChange);
    }

    public AbstractRecordCommand getInsert(DataTable table, EntityChange entityChange, Supplier<CreateRecordCommand> commandCreator) {
        //noinspection RedundantTypeArguments IntelliJ fails to compile without specification
        return getOrCreate(inserts, IdToCommandMap<CreateRecordCommand>::new, commandCreator, table, entityChange);
    }

    public AbstractRecordCommand getInsertOnDuplicateUpdate(DataTable table, EntityChange entityChange, Supplier<CreateRecordCommand> commandCreator) {
        //noinspection RedundantTypeArguments IntelliJ fails to compile without specification
        return getOrCreate(insertsOnDuplicateUpdate, IdToCommandMap<CreateRecordCommand>::new, commandCreator, table, entityChange);
    }

    public void commit(CommandsExecutor commandsExecutor, PersistentLayerStats stats) {
        for (Map.Entry<DataTable, IdToCommandMap< DeleteRecordCommand>> entry : deletes.entrySet()) {
            DataTable table = entry.getKey();
            AffectedRows affectedRows = commandsExecutor.executeDeletes(table, entry.getValue().map.values());
            stats.addAffectedRows(table.getName(), affectedRows);
        }
        for (Map.Entry<DataTable, IdToCommandMap<CreateRecordCommand>> entry : inserts.entrySet()) {
            DataTable table = entry.getKey();
            Collection<CreateRecordCommand> commands = entry.getValue().map.values();
            AffectedRows affectedRows;
            switch (onDuplicateKey) {
                case IGNORE:
                    affectedRows = commandsExecutor.executeInsertsOnDuplicateKeyIgnore(table, commands);
                    break;
                case UPDATE:
                    affectedRows = commandsExecutor.executeInsertsOnDuplicateKeyUpdate(table, commands);
                    break;
                case FAIL:
                default:
                    affectedRows = commandsExecutor.executeInserts(table, commands);
                    break;
            }
            stats.addAffectedRows(table.getName(), affectedRows);
        }
        for (Map.Entry<DataTable, IdToCommandMap< UpdateRecordCommand>> entry : updates.entrySet()) {
            DataTable table = entry.getKey();
            AffectedRows affectedRows = commandsExecutor.executeUpdates(table, entry.getValue().map.values());
            stats.addAffectedRows(table.getName(), affectedRows);
        }
        for (Map.Entry<DataTable, IdToCommandMap<CreateRecordCommand>> entry : insertsOnDuplicateUpdate.entrySet()) {
            DataTable table = entry.getKey();
            AffectedRows affectedRows = commandsExecutor.executeInsertsOnDuplicateKeyUpdate(table, entry.getValue().map.values());
            stats.addAffectedRows(table.getName(), affectedRows);
        }
    }

     private <RC extends AbstractRecordCommand> AbstractRecordCommand getOrCreate(Map<DataTable, IdToCommandMap< RC>> map, Supplier<IdToCommandMap< RC>> creator, Supplier<RC> commandCreator, DataTable table, EntityChange entityChange) {
        //noinspection unchecked
         IdToCommandMap< RC> idToCommandMap = map.computeIfAbsent(table, k -> creator.get());
         return idToCommandMap.getOrCreate(entityChange, commandCreator);
    }

    private static class IdToCommandMap<RC extends AbstractRecordCommand> {
        private final Map<EntityChange, RC> map = new HashMap<>();

        public AbstractRecordCommand getOrCreate(EntityChange entityChange, Supplier<RC> commandCreator) {
            return map.computeIfAbsent(entityChange, k -> commandCreator.get());
        }
    }
}


