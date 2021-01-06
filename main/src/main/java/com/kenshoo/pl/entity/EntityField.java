package com.kenshoo.pl.entity;

import org.jooq.Record;
import org.jooq.TableField;

import java.util.Arrays;

public interface EntityField<E extends EntityType<E>, T> {

    EntityFieldDbAdapter<T> getDbAdapter();

    ValueConverter<T, String> getStringValueConverter();

    boolean valuesEqual(T v1, T v2);

    default Class<T> getValueClass() {
        return getStringValueConverter().getValueClass();
    }

    default boolean isVirtual() {
        return false;
    }

    EntityType<E> getEntityType();

    default PLCondition eq(T value) {
        if (isVirtual()) {
            throw new UnsupportedOperationException("The equals operation is unsupported for virtual fields");
        }
        final Object tableValue = getDbAdapter().getFirstDbValue(value);
        @SuppressWarnings("unchecked")
        final TableField<Record, Object> tableField = (TableField<Record, Object>)getDbAdapter().getFirstTableField();
        return new PLCondition(tableField.eq(tableValue), this);
    }

    default PLCondition in(T ...values) {
        if (isVirtual()) {
            throw new UnsupportedOperationException("The in operation is unsupported for virtual fields");
        }

        final Object[] tableValues = Arrays.stream(values).map(value -> getDbAdapter().getFirstDbValue(value)).toArray(Object[]::new);
        @SuppressWarnings("unchecked")
        final TableField<Record, Object> tableField = (TableField<Record, Object>)getDbAdapter().getFirstTableField();
        return new PLCondition(tableField.in(tableValues), this);
    }

    default PLCondition isNull() {
        if (isVirtual()) {
            throw new UnsupportedOperationException("The equals operation is unsupported for virtual fields");
        }
        @SuppressWarnings("unchecked")
        final TableField<Record, Object> tableField = (TableField<Record, Object>)getDbAdapter().getFirstTableField();
        return new PLCondition(tableField.isNull(), this);
    }
}
