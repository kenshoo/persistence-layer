package com.kenshoo.pl.entity;

import org.jooq.Record;
import org.jooq.TableField;

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

    default TableField<Record, ?> findFirstTableField() {
        return getDbAdapter().getTableFields()
                             .findFirst()
                             .orElseThrow(() -> new IllegalStateException("No table fields found for an entity field"));
    }
}
