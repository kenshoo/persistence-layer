package com.kenshoo.pl.data;

import org.apache.commons.lang3.ArrayUtils;
import org.jooq.TableField;
import java.util.Arrays;


public class DatabaseId {

    private final TableField<?, ?>[] tableFields;
    private final Object[] values;
    private int hashCode;

    public DatabaseId(TableField<?, ?>[] tableFields, Object[] values) {
        this.values = values;
        this.tableFields = tableFields;
    }

    public DatabaseId append(DatabaseId other) {
        return new DatabaseId(
                ArrayUtils.addAll(this.tableFields, other.tableFields),
                ArrayUtils.addAll(this.values, other.values)
        );
    }

    public Object[] getValues() {
        return values;
    }

    public static <T> DatabaseId create(TableField<?, T> tableField, T val) {
        return new SingleDBId<>(tableField, val);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DatabaseId)) return false;

        DatabaseId that = (DatabaseId) o;

        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.deepEquals(values, that.values);

    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = Arrays.deepHashCode(values);
        }
        return hashCode;
    }

    public TableField<?, ?>[] getTableFields() {
        return tableFields;
    }
}
