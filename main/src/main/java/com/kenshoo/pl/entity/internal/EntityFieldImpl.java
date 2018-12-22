package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityFieldDbAdapter;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.ValueConverter;
import com.kenshoo.pl.entity.equalityfunctions.EntityValueEqualityFunction;

public class EntityFieldImpl<E extends EntityType<E>, T> implements EntityField<E, T> {

    private final EntityType<E> entityType;

    private final EntityFieldDbAdapter<T> dbAdapter;

    private final EntityValueEqualityFunction<T> valueEqualityFunction;

    private final ValueConverter<T, String> stringValueConverter;

    public EntityFieldImpl(EntityType<E> entityType, EntityFieldDbAdapter<T> dbAdapter, ValueConverter<T, String> stringValueConverter, EntityValueEqualityFunction<T> valueEqualityFunction) {
        this.entityType = entityType;
        this.dbAdapter = dbAdapter;
        this.valueEqualityFunction = valueEqualityFunction;
        this.stringValueConverter = stringValueConverter;
    }

    @Override
    public EntityFieldDbAdapter<T> getDbAdapter() {
        return dbAdapter;
    }

    @Override
    public ValueConverter<T, String> getStringValueConverter() {
        return stringValueConverter;
    }

    @Override
    public boolean valuesEqual(T v1, T v2) {
        return valueEqualityFunction.apply(v1, v2);
    }

    @Override
    public String toString() {
        return entityType.toFieldName(this);
    }

    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(Object o) {
        return (this == o);
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }
}
