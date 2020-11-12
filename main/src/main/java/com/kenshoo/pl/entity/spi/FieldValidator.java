package com.kenshoo.pl.entity.spi;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.ValidationError;

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
     * Validates the value.
     *
     * @param fieldValue new value of the field
     * @return validation error or <code>null</code> if none
     */
    ValidationError validate(T fieldValue);
}
