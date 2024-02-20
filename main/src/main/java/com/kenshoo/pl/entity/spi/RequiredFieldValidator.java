package com.kenshoo.pl.entity.spi;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.FinalEntityState;

import java.util.function.Predicate;
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
     * The predicate is evaluated on the final state of the entity See {@link FinalEntityState}.
     * @return a predicate indicating when the field should be validated. It will be evaluated together with {@link #fetchFields()},
     * which means that all the parent fields appearing in the predicate must also be included in the fields to fetch or be required
     * for create operation
     */
    default Predicate<FinalEntityState> requireWhen() {
        return e -> true;
    }

    /**
     * @return a list of fields to fetch.
     */
    default Stream<EntityField<?, ?>> fetchFields() {
        return Stream.of();
    }
}
