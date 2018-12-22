package com.kenshoo.pl.jooq;

import org.jooq.Record;
import org.jooq.TableField;

public class FieldAndValue<T> {

    private final TableField<Record, T> field;
    private final T value;

    public FieldAndValue(TableField<Record, T> field, T value) {
        this.field = field;
        this.value = value;
    }

    public TableField<Record, T> getField() {
        return field;
    }

    public T getValue() {
        return value;
    }
}
