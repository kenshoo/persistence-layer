package com.kenshoo.pl.entity.spi;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.FieldsCombination;

import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A validator that verifies that the resulting state of several fields is legal, no matter whether some of them
 * are already set or being modified by the command. For instance, if a validator validates that the value of field 'B'
 * is greater than the value of field 'A', it is going to be called when either 'B' or 'A' (or both) are being changed.
 */
public interface FieldsCombinationValidator<E extends EntityType<E>> extends ChangeValidator {

    /**
     * @return the list of fields whose combination is being validated; May contain fields of the current entity only.
     */
    Stream<EntityField<E, ?>> validatedFields();

    /**
     * Implements the validation and return an error if there is.
     *
     * @param fieldsCombination the map containing the fields specified by {@link #validatedFields()}. If a field is
     *                       being modified by the command, its new value is going to be in the map, otherwise the
     *                       existing value would be passed.
     * @return validation error if there is or <code>null</code> if OK
     */
    ValidationError validate(FieldsCombination<E> fieldsCombination);

    /**
     * @return a list of fields to fetch. May contain fields of parent entities only
     */
    default Stream<EntityField<?, ?>> fetchFields() {
        return Stream.of();
    }

    /**
     * The predicate is evaluated on the final state of the entity See {@link FinalEntityState}.
     * @return a predicate indicating when the field should be validated. It will be evaluated together with {@link #fetchFields()},
     * which means that all the fields appearing in the predicate must also be included in the fields to fetch, validated fields or be required
     * for create operation
     */
    default Predicate<FinalEntityState> validateWhen() {
        return e -> true;
    }
}
