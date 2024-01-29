package com.kenshoo.pl.entity;

import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.lambda.Seq;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

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
        return new PLCondition(tableField.eq(tableValue), entity -> entity.safeGet(this).equalsValue(value), this);
    }

    default PLCondition eq(EntityField<?, T> otherField) {
        if (isVirtual()) {
            throw new UnsupportedOperationException("The equals operation is unsupported for virtual fields");
        }

        @SuppressWarnings("unchecked")
        final TableField<Record, T> tableField = (TableField<Record, T>)getDbAdapter().getFirstTableField();
        @SuppressWarnings("unchecked")
        final TableField<Record, T> otherTableField = (TableField<Record, T>)otherField.getDbAdapter().getFirstTableField();
        return new PLCondition(tableField.eq(otherTableField), entity -> entity.safeGet(this).equalsValue(entity.safeGet(otherField).get()), this, otherField);
    }

    default PLCondition in(T ...values) {
        if (isVirtual()) {
            throw new UnsupportedOperationException("The in operation is unsupported for virtual fields");
        }

        final Object[] tableValues = Arrays.stream(values).map(value -> getDbAdapter().getFirstDbValue(value)).toArray(Object[]::new);
        @SuppressWarnings("unchecked")
        final TableField<Record, Object> tableField = (TableField<Record, Object>)getDbAdapter().getFirstTableField();
        final var setOfValues = Seq.of(values).collect(Collectors.toUnmodifiableSet());
        return new PLCondition(tableField.in(tableValues),
                entity -> entity.safeGet(this).filter(Objects::nonNull).matches(setOfValues::contains), this);
    }

    default PLCondition isNull() {
        if (isVirtual()) {
            throw new UnsupportedOperationException("The equals operation is unsupported for virtual fields");
        }
        @SuppressWarnings("unchecked")
        final TableField<Record, Object> tableField = (TableField<Record, Object>)getDbAdapter().getFirstTableField();
        return new PLCondition(tableField.isNull(), entity -> entity.safeGet(this).isNull(), this);
    }

    default PLCondition isNotNull() {
        if (isVirtual()) {
            throw new UnsupportedOperationException("The equals operation is unsupported for virtual fields");
        }
        @SuppressWarnings("unchecked")
        final TableField<Record, Object> tableField = (TableField<Record, Object>)getDbAdapter().getFirstTableField();
        return new PLCondition(tableField.isNotNull(), entity -> entity.safeGet(this).isNotNull(), this);
    }
}
