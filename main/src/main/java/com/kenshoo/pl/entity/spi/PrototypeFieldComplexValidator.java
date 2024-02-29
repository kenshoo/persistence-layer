package com.kenshoo.pl.entity.spi;

import com.kenshoo.pl.entity.*;

import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Same as {@link FieldComplexValidator} but operating in terms of "prototypes".
 * @param <T>
 */
public interface PrototypeFieldComplexValidator<T> extends ChangeValidator {

    /**
     * @return the field prototype validated by this validator
     */
    EntityFieldPrototype<T> getPrototype();

    /**
     * Validates the new value for the field. The implementation can query the <code>entity</code> only for
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
    default Stream<EntityField<?, ?>> fetchFields() { return Stream.of(); }

    /**
     * The predicate is evaluated on the final state of the entity See {@link FinalEntityState}.
     * @return a predicate indicating when the field should be validated. It will be evaluated together with {@link #fetchFields()},
     * which means that all the parent fields appearing in the predicate must also be included in the fields to fetch.
     */
    default Predicate<CurrentEntityState> validateWhen() {
        return e -> true;
    }
}
