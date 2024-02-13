package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.EntityFieldDbAdapter;
import com.kenshoo.pl.entity.EntityFieldPrototype;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.PrototypedEntityField;
import com.kenshoo.pl.entity.ValueConverter;
import com.kenshoo.pl.entity.equalityfunctions.EntityValueEqualityFunction;

import java.util.Comparator;

public class PrototypedEntityFieldImpl<E extends EntityType<E>, T>
        extends AbstractEntityField<E, T, PrototypedEntityFieldImpl<E, T>, PrototypedEntityFieldImpl.Builder<E, T>>
        implements PrototypedEntityField<E, T> {

    private final EntityFieldPrototype<T> entityFieldPrototype;

    private PrototypedEntityFieldImpl(final EntityType<E> entityType,
                                      final EntityFieldPrototype<T> entityFieldPrototype,
                                      final EntityFieldDbAdapter<T> dbAdapter,
                                      final ValueConverter<T, String> stringValueConverter,
                                      final EntityValueEqualityFunction<T> valueEqualityFunction,
                                      final Comparator<T> valueComparator) {
        super(entityType, dbAdapter, stringValueConverter, valueEqualityFunction, valueComparator);
        this.entityFieldPrototype = entityFieldPrototype;
    }

    @Override
    public EntityFieldPrototype<T> getEntityFieldPrototype() {
        return entityFieldPrototype;
    }

    @Override
    protected Builder<E, T> toBuilder() {
        return super.toBuilder()
                .withEntityFieldPrototype(entityFieldPrototype);
    }

    @Override
    protected Builder<E, T> newBuilder(final EntityType<E> entityType) {
        return new PrototypedEntityFieldImpl.Builder<>(entityType);
    }

    public static <E extends EntityType<E>, T> Builder<E, T> builder(final EntityType<E> entityType) {
        return new Builder<>(entityType);
    }

    public static class Builder<E extends EntityType<E>, T>
            extends AbstractEntityField.Builder<E, T, PrototypedEntityFieldImpl<E, T>, PrototypedEntityFieldImpl.Builder<E, T>> {

        private EntityFieldPrototype<T> entityFieldPrototype;

        public Builder(final EntityType<E> entityType) {
            super(entityType);
        }

        protected PrototypedEntityFieldImpl.Builder<E, T> self() {
            return this;
        }

        public Builder<E, T> withEntityFieldPrototype(final EntityFieldPrototype<T> entityFieldPrototype) {
            this.entityFieldPrototype = entityFieldPrototype;
            return this;
        }

        public PrototypedEntityFieldImpl<E, T> build() {
            return new PrototypedEntityFieldImpl<>(
                    entityType,
                    entityFieldPrototype,
                    dbAdapter,
                    stringValueConverter,
                    valueEqualityFunction,
                    valueComparator
            );
        }
    }
}
