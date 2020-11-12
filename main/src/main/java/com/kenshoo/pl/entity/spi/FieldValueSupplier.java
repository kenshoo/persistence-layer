package com.kenshoo.pl.entity.spi;

import com.kenshoo.pl.entity.ChangeOperation;
import com.kenshoo.pl.entity.CurrentEntityState;
import com.kenshoo.pl.entity.EntityField;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A "delayed" supplier of the new value for a field that decides on the value given the current state. For example,
 * a logic to increment the current value by 10% can be implemented by implementing this interface. The {@link #supply(CurrentEntityState)}
 * method is going to be called after the current state has been fetched from the database.
 *
 * @param <T> type of the field whose value is being supplied
 * @see com.kenshoo.pl.entity.ChangeEntityCommand#set(EntityField, FieldValueSupplier)
 */
public interface FieldValueSupplier<T> extends FetchEntityFields {

    /**
     * Returns the new value for a field given an existing entity
     *
     * @param currentState entity before the change
     * @return new field value
     * @throws ValidationException if the supposed change is invalid
     * @throws NotSuppliedException if the supplier doesn't want to change the current value
     */
    T supply(CurrentEntityState currentState) throws ValidationException, NotSuppliedException;

    static <OLD_VAL, NEW_VAL> FieldValueSupplier<NEW_VAL> fromOldValue(EntityField<?, OLD_VAL> field, Function<OLD_VAL, NEW_VAL> func) {
        return new FieldValueSupplier<NEW_VAL>() {
            @Override
            public NEW_VAL supply(CurrentEntityState oldState) throws ValidationException, NotSuppliedException {
                return func.apply(oldState.get(field));
            }
            @Override
            public Stream<EntityField<?, ?>> fetchFields(ChangeOperation changeOperation) {
                return Stream.of(field);
            }
        };
    }

    static <T1, T2, RES> FieldValueSupplier<RES> fromValues(EntityField<?, T1> field1, EntityField<?, T2> field2, BiFunction<T1, T2, RES> func) {
        return new FieldValueSupplier<RES>() {
            @Override
            public RES supply(CurrentEntityState oldState) throws ValidationException, NotSuppliedException {
                return func.apply(oldState.get(field1), oldState.get(field2));
            }
            @Override
            public Stream<EntityField<?, ?>> fetchFields(ChangeOperation changeOperation) {
                return Stream.of(field1, field2);
            }
        };
    }


}
