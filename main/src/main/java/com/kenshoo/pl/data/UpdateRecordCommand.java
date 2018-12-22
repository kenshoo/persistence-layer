package com.kenshoo.pl.data;


import com.kenshoo.pl.jooq.DataTable;
import org.jooq.Record;
import org.jooq.TableField;

public class UpdateRecordCommand extends AbstractRecordCommand {

    private final DatabaseId id;

    public UpdateRecordCommand(DataTable table, DatabaseId id) {
        super(table);
        this.id = id;
    }

    /**
     * Convenience constructor for tables with integer ID
     */
    public UpdateRecordCommand(DataTable table, int id) {
        super(table);
        TableField<Record, ?> primaryKeyField = getPrimaryKeyField(table, Integer.class);
        //noinspection unchecked
        this.id = DatabaseId.create((TableField<Record, Integer>) primaryKeyField, id);
    }

    /**
     * Convenience constructor for tables with bigint ID
     */
    public UpdateRecordCommand(DataTable table, long id) {
        super(table);
        TableField<Record, ?> primaryKeyField = getPrimaryKeyField(table, Long.class);
        //noinspection unchecked
        this.id = DatabaseId.create((TableField<Record, Long>) primaryKeyField, id);
    }

    public DatabaseId getId() {
        return id;
    }
}
