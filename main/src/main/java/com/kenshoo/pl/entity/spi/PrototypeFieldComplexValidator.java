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
     * the fields it has declared in {@link #ancestorsFields()}.
     *
     * @param fieldValue new value of the field
     * @param currentState existing entity
     *
     * @return a validation error if any, <code>null</code> if none
     */
    ValidationError validate(T fieldValue, CurrentEntityState currentState);

    /**
     * @return ancestor entities fields to fetch.
     */
    default Stream<EntityField<?, ?>> ancestorsFields() { return Stream.of(); }

    /**
     * The predicate is evaluated on the final state of the entity See {@link FinalEntityState}.
     * @return a predicate indicating when the field should be validated. It will be evaluated together with {@link #ancestorsFields()},
     * which means that all the ancestor fields appearing in the predicate must also be included in the response of {@link #ancestorsFields()}.
     */
    default Predicate<CurrentEntityState> validateWhen() {
        return e -> true;
    }
}
