package com.kenshoo.pl.entity.spi;

import com.kenshoo.pl.entity.EntityFieldPrototype;
import com.kenshoo.pl.entity.ValidationError;

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
}
