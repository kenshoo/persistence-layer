package com.kenshoo.pl.entity.spi;

import com.kenshoo.pl.entity.*;

import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Same as {@link FieldValidator} but for a field prototype.
 *
 * @param <T> entity type
 */
public interface PrototypeFieldValidator<T> extends ChangeValidator {

    /**
     * @return the field prototype validated by the validator
     */
    EntityFieldPrototype<T> getPrototype();

    /**
     * Validates the value.
     *
     * @param value new value of the field
     * @return validation error or <code>null</code> if none
     */
    ValidationError validate(T value);

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
