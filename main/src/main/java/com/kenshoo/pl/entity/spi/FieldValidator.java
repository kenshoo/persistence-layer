package com.kenshoo.pl.entity.spi;

import com.kenshoo.pl.entity.*;

import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * The simplest form of a validator - validates a single field value without using any external information.
 * For example, may be used to validate that the value of a field is positive.
 *
 * @param <E> entity type
 * @param <T> type of the validated field
 */
public interface FieldValidator<E extends EntityType<E>, T> extends ChangeValidator {

    /**
     * @return the field validated by the validator
     */
    EntityField<E, T> validatedField();

    /**
     * Validates the new field value.
     *
     * @param fieldValue new value of the field
     * @return validation error or <code>null</code> if none
     */
    ValidationError validate(T fieldValue);

    /**
     * @return a list of fields to fetch. May contain fields of parent entities only
     */
    default Stream<EntityField<?, ?>> fetchFields() {
        return Stream.of();
    }

    /**
     * The predicate is evaluated on the final state of the entity See {@link FinalEntityState}.
     * @return a predicate indicating when the field should be validated. It will be evaluated together with {@link #fetchFields()},
     * which means that all the fields appearing in the predicate must also be included in the fields to fetch or be required
     * for create operation
     */
    default Predicate<FinalEntityState> validateWhen() {
        return e -> true;
    }
}
