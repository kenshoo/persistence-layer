package com.kenshoo.pl.data;

import com.kenshoo.pl.jooq.DataTable;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.TableField;

public class DeleteRecordCommand extends AbstractRecordCommand {

    private final DatabaseId id;

    public DeleteRecordCommand(DataTable table, DatabaseId id) {
        super(table);
        this.id = id;
    }

    /**
     * Convenience constructor for tables with integer ID
     */
    public DeleteRecordCommand(DataTable table, int id) {
        super(table);
        TableField<Record, ?> primaryKeyField = getPrimaryKeyField(table, Integer.class);
        //noinspection unchecked
        this.id = DatabaseId.create((TableField<Record, Integer>) primaryKeyField, id);
    }

    /**
     * Convenience constructor for tables with bigint ID
     */
    public DeleteRecordCommand(DataTable table, long id) {
        super(table);
        TableField<Record, ?> primaryKeyField = getPrimaryKeyField(table, Long.class);
        //noinspection unchecked
        this.id = DatabaseId.create((TableField<Record, Long>) primaryKeyField, id);
    }

    public DatabaseId getId() {
        return id;
    }

    @Override
    public <T> void set(Field<T> field, T value) {
        throw new UnsupportedOperationException("Setting a field is not supported for a delete command");
    }
}
