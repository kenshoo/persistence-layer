package com.kenshoo.pl.entity.spi;

import com.kenshoo.pl.entity.CurrentEntityState;
import com.kenshoo.pl.entity.EntityChange;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;

import java.util.function.BiPredicate;
import java.util.stream.Stream;

/**
 * A validator that force given a value in create command
 *
 * @param <E> entity type
 * @param <T> required field type
 */
public interface RequiredFieldValidator<E extends EntityType<E>, T> extends ChangeValidator {

    /**
     * @return the field that have to be
     */
    EntityField<E, T> requiredField();

    /**
     * @return the error code to return if the field is missing
     */
    String getErrorCode();

    /**
     * @return Predicate when should validate field. It is used together with fetchFields()
     */
    default BiPredicate<CurrentEntityState, EntityChange<E>> requireWhen() {
        return (currentState, entityChange) -> true;
    }

    /**
     * @return a list of fields to fetch.
     */
    default Stream<EntityField<?, ?>> fetchFields() {
        return Stream.of();
    }
}
