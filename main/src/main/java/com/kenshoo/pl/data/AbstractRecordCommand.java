package com.kenshoo.pl.data;

import com.kenshoo.jooq.DataTable;
import gnu.trove.map.hash.THashMap;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.UniqueKey;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public abstract class AbstractRecordCommand {

    private final DataTable table;
    private final Map<Field<?>, Object> values = new THashMap<>();

    protected AbstractRecordCommand(DataTable table) {
        this.table = table;
    }

    public <T> void set(Field<T> field, T value) {
        values.put(field, value);
    }

    public Stream<Field<?>> getFields() {
        return values.keySet().stream();
    }

    public Stream<Object> getValues(Stream<Field<?>> fields) {
        return fields.map(values::get);
    }

    public DataTable getTable() {
        return table;
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    protected static TableField<Record, ?> getPrimaryKeyField(DataTable table, Class dataType) {
        UniqueKey<Record> primaryKey = table.getPrimaryKey();
        if (primaryKey == null) {
            throw new IllegalArgumentException("Table " + table + " doesn't have a primary key");
        }
        List<TableField<Record, ?>> primaryKeyFields = primaryKey.getFields();
        TableField<Record, ?> primaryKeyField = primaryKeyFields.get(0);
        if (primaryKeyFields.size() > 1 || primaryKeyField.getType() != dataType) {
            throw new IllegalArgumentException("Primary key of table " + table.getName() + " is not of type " + dataType.getSimpleName());
        }
        return primaryKeyField;
    }
}
