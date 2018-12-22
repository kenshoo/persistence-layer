package com.kenshoo.pl.entity.spi;

import com.kenshoo.pl.entity.EntityFieldPrototype;
import com.kenshoo.pl.entity.PrototypeFieldsCombination;
import com.kenshoo.pl.entity.ValidationError;

import java.util.Collection;

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
}
