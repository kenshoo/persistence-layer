package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.EntityFieldDbAdapter;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.ValueConverter;
import com.kenshoo.pl.entity.equalityfunctions.EntityValueEqualityFunction;

import java.util.Comparator;

public class VirtualEntityFieldImpl<E extends EntityType<E>, T> extends
        AbstractEntityField<E, T, VirtualEntityFieldImpl<E, T>, VirtualEntityFieldImpl.Builder<E, T>> {

    private VirtualEntityFieldImpl(final EntityType<E> entityType,
                                   final EntityFieldDbAdapter<T> dbAdapter,
                                   final ValueConverter<T, String> stringValueConverter,
                                   final EntityValueEqualityFunction<T> valueEqualityFunction,
                                   final Comparator<T> valueComparator) {
        super(entityType, dbAdapter, stringValueConverter, valueEqualityFunction, valueComparator);
    }


    @Override
    public boolean isVirtual() {
        return true;
    }

    @Override
    protected Builder<E, T> newBuilder(final EntityType<E> entityType) {
        return new Builder<>(entityType);
    }

    public static <E extends EntityType<E>, T> Builder<E, T> builder(final EntityType<E> entityType) {
        return new Builder<>(entityType);
    }

    public static class Builder<E extends EntityType<E>, T>
            extends AbstractEntityField.Builder<E, T, VirtualEntityFieldImpl<E, T>, Builder<E, T>> {

        public Builder(final EntityType<E> entityType) {
            super(entityType);
        }

        protected Builder<E, T> self() {
            return this;
        }

        public VirtualEntityFieldImpl<E, T> build() {
            return new VirtualEntityFieldImpl<>(
                    entityType,
                    dbAdapter,
                    stringValueConverter,
                    valueEqualityFunction,
                    valueComparator);
        }
    }
}
