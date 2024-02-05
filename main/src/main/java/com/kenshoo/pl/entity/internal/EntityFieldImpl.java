package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.equalityfunctions.EntityValueEqualityFunction;

import java.util.Comparator;

public class EntityFieldImpl<E extends EntityType<E>, T>
        extends AbstractEntityField<E, T, EntityFieldImpl<E, T>, EntityFieldImpl.Builder<E, T>> {

    protected EntityFieldImpl(final EntityType<E> entityType,
                              final EntityFieldDbAdapter<T> dbAdapter,
                              final ValueConverter<T, String> stringValueConverter,
                              final EntityValueEqualityFunction<T> valueEqualityFunction,
                              final Comparator<T> valueComparator) {
        super(entityType, dbAdapter, stringValueConverter, valueEqualityFunction, valueComparator);
    }

    @Override
    protected Builder<E, T> newBuilder(final EntityType<E> entityType) {
        return new Builder<>(entityType);
    }

    public static <E extends EntityType<E>, T> Builder<E, T> builder(final EntityType<E> entityType) {
        return new Builder<>(entityType);
    }

    public static class Builder<E extends EntityType<E>, T>
            extends AbstractEntityField.Builder<E, T, EntityFieldImpl<E, T>, Builder<E, T>> {

        public Builder(final EntityType<E> entityType) {
            super(entityType);
        }

        protected Builder<E, T> self() {
            return this;
        }

        public EntityFieldImpl<E, T> build() {
            return new EntityFieldImpl<>(
                    entityType,
                    dbAdapter,
                    stringValueConverter,
                    valueEqualityFunction,
                    valueComparator);
        }
    }
}
