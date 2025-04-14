package com.kenshoo.pl.entity;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.TableField;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

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
        return compareTo(value, Field::eq);
    }

    default PLPostFetchCondition postFetchEq(T otherValue) {
        return postFetchCompareWith(thisTriptional -> valuesEqual(thisTriptional, otherValue));
    }

    default PLPostFetchCondition postFetchEq(EntityField<?, T> otherField) {
        final Predicate<Entity> predicate =
                entity -> entity.safeGet(this).matches(thisValue -> valuesEqual(entity.safeGet(otherField), thisValue));
        return new PLPostFetchCondition(predicate, this, otherField);
    }

    @SuppressWarnings("unchecked")
    default PLCondition in(T ...values) {
        return compareTo(Arrays.stream(values), Field::in);
    }

    @SuppressWarnings("unchecked")
    default PLPostFetchCondition postFetchIn(T ...values) {
        final var setOfValues = Set.of(values);
        return postFetchCompareWith(thisTriptional -> thisTriptional.filter(Objects::nonNull).matches(setOfValues::contains));
    }

    default PLCondition isNull() {
        final var tableField = extractFirstTableField();
        return new PLCondition(tableField.isNull(), this);
    }

    default PLPostFetchCondition postFetchIsNull() {
        return postFetchCompareWith(Triptional::isNull);
    }

    default PLCondition isNotNull() {
        final var tableField = extractFirstTableField();
        return new PLCondition(tableField.isNotNull(), this);
    }

    default PLPostFetchCondition postFetchIsNotNull() {
        return postFetchCompareWith(Triptional::isNotNull);
    }

    private PLCondition compareTo(final T value,
                                  final BiFunction<TableField<Record, Object>, Object, Condition> conditionGenerator) {
        final var tableField = extractFirstTableField();
        final Object tableValue = getDbAdapter().getFirstDbValue(value);
        return new PLCondition(conditionGenerator.apply(tableField, tableValue), this);
    }

    private PLCondition compareTo(final Stream<T> values,
                                  final BiFunction<TableField<Record, Object>, Object[], Condition> conditionGenerator) {
        final var tableField = extractFirstTableField();
        final Object[] tableValues = values.map(value -> getDbAdapter().getFirstDbValue(value)).toArray(Object[]::new);
        return new PLCondition(conditionGenerator.apply(tableField, tableValues), this);
    }

    private PLPostFetchCondition postFetchCompareWith(final Predicate<Triptional<T>> predicate) {
        return new PLPostFetchCondition(entity -> predicate.test(entity.safeGet(this)), this);
    }

    private TableField<Record, Object> extractFirstTableField() {
        if (isVirtual()) {
            throw new UnsupportedOperationException("PLConditions cannot be built for virtual fields");
        }
        //noinspection unchecked
        return (TableField<Record, Object>)getDbAdapter().getFirstTableField();
    }

    private boolean valuesEqual(final Triptional<T> thisTriptional, final T otherValue) {
        return thisTriptional.matches(thisValue -> valuesEqual(thisValue, otherValue));
    }
}
