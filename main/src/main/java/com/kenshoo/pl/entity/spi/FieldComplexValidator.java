package com.kenshoo.pl.entity.spi;

import com.kenshoo.pl.entity.*;

import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A validator that checks one field and uses parent entity fields for the verification.
 *
 * @param <E> entity type
 * @param <T> data type of the field being validated
 */
public interface FieldComplexValidator<E extends EntityType<E>, T> extends ChangeValidator {

    /**
     * @return the field validated by this validator
     */
    EntityField<E, T> validatedField();

    /**
     * Called by the framework to validate the value. The implementation can query the <code>entity</code> only for
     * the fields it has declared in {@link #fetchFields()}.
     *
     * @param fieldValue new value of the field
     * @param currentState existing entity
     *
     * @return a validation error if any, <code>null</code> if none
     */
    ValidationError validate(T fieldValue, CurrentEntityState currentState);

    /**
     * @return a list of fields to fetch. Can contain only parent entities fields.
     */
    Stream<EntityField<?, ?>> fetchFields();

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
