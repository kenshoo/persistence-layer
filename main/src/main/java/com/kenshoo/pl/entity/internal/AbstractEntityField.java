package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.equalityfunctions.EntityValueEqualityFunction;

import java.util.Comparator;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

abstract class AbstractEntityField<
        E extends EntityType<E>,
        T,
        F extends EntityField<E, T>,
        B extends AbstractEntityField.Builder<E, T, F, B>> implements MutableEntityField<E, T> {

    protected final EntityType<E> entityType;

    protected final EntityFieldDbAdapter<T> dbAdapter;

    protected final EntityValueEqualityFunction<T> valueEqualityFunction;

    protected final Comparator<T> valueComparator;

    protected final ValueConverter<T, String> stringValueConverter;

    protected AbstractEntityField(final EntityType<E> entityType,
                                  final EntityFieldDbAdapter<T> dbAdapter,
                                  final ValueConverter<T, String> stringValueConverter,
                                  final EntityValueEqualityFunction<T> valueEqualityFunction,
                                  final Comparator<T> valueComparator) {
        this.entityType = entityType;
        this.dbAdapter = dbAdapter;
        this.stringValueConverter = stringValueConverter;
        this.valueEqualityFunction = valueEqualityFunction;
        this.valueComparator = valueComparator;
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
    public int compareValues(final T v1, final T v2) {
        return comparatorOf(v1).compare(v1, v2);
    }

    @Override
    public EntityType<E> getEntityType() {
        return entityType;
    }

    protected B toBuilder() {
        return newBuilder(entityType)
                .withDbAdapter(dbAdapter)
                .withValueEqualityFunction(valueEqualityFunction)
                .withValueComparator(valueComparator)
                .withStringValueConverter(stringValueConverter);
    }

    protected abstract B newBuilder(final EntityType<E> entityType);

    @Override
    public EntityField<E, T> comparedBy(final Comparator<T> valueComparator) {
        requireNonNull(valueComparator);
        return toBuilder()
                .withValueComparator(valueComparator)
                .withValueEqualityFunction(equalityFunctionOf(valueComparator))
                .build();
    }

    @Override
    public String toString() {
        return entityType.toFieldName(this);
    }

    private Comparator<T> comparatorOf(T value) {
        return Optional.ofNullable(valueComparator)
                .orElseGet(() -> {
                    if (value instanceof Comparable<?>) {
                        //noinspection unchecked
                        return (Comparator<T>) Comparator.naturalOrder();
                    }
                    throw new UnsupportedOperationException(String.format("The field [%s] is not comparable for the value [%s]", this, value));
                });
    }


    private EntityValueEqualityFunction<T> equalityFunctionOf(final Comparator<T> valueComparator) {
        return (v1, v2) -> valueComparator.compare(v1, v2) == 0;
    }

    protected abstract static class Builder<
            E extends EntityType<E>,
            T,
            F extends EntityField<E, T>,
            B extends Builder<E, T, F, B>> {

        protected final EntityType<E> entityType;

        protected EntityFieldDbAdapter<T> dbAdapter;

        protected EntityValueEqualityFunction<T> valueEqualityFunction;

        protected Comparator<T> valueComparator;

        protected ValueConverter<T, String> stringValueConverter;

        protected Builder(final EntityType<E> entityType) {
            this.entityType = entityType;
        }

        public B withDbAdapter(final EntityFieldDbAdapter<T> dbAdapter) {
            this.dbAdapter = dbAdapter;
            return self();
        }

        public B withValueEqualityFunction(final EntityValueEqualityFunction<T> valueEqualityFunction) {
            this.valueEqualityFunction = valueEqualityFunction;
            return self();
        }

        public B withValueComparator(final Comparator<T> valueComparator) {
            this.valueComparator = valueComparator;
            return self();
        }

        public B withStringValueConverter(final ValueConverter<T, String> stringValueConverter) {
            this.stringValueConverter = stringValueConverter;
            return self();
        }

        protected abstract B self();

        public abstract F build();
    }
}
