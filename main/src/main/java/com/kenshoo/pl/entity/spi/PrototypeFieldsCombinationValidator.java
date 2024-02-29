package com.kenshoo.pl.entity.spi;

import com.kenshoo.pl.entity.*;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Same as {@link FieldsCombinationValidator} but defined in terms of field prototypes.
 */
public interface PrototypeFieldsCombinationValidator extends ChangeValidator {

    /**
     * @return the list of fields whose combination is being validated
     */
    Collection<EntityFieldPrototype<?>> getPrototypes();

    /**
     * Implements the validation and return an error if there is.
     *
     * @param fieldsCombination the map containing the fields specified by {@link #getPrototypes()}. If a field is
     *                          being modified by the command, its new value is going to be in the map, otherwise the
     *                          existing value would be passed.
     * @return validation error if there is or <code>null</code> if OK
     */
    ValidationError validate(PrototypeFieldsCombination<?> fieldsCombination);

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
