package com.kenshoo.pl.jooq;

import org.jooq.Field;

import java.util.Collection;

public class FieldAndValues<T> {
    private final Field<T> field;
    private final Collection<? extends T> values;

    public FieldAndValues(Field<T> field, Collection<? extends T> values) {
        this.field = field;
        this.values = values;
    }

    public Field<T> getField() {
        return field;
    }

    public Collection<? extends T> getValues() {
        return values;
    }
}
