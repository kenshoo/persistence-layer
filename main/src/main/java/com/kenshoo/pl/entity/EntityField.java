package com.kenshoo.pl.entity;

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
}
