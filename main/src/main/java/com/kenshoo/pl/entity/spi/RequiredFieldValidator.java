package com.kenshoo.pl.entity.spi;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;

/**
 * A validator that force given a value in create command
 *
 * @param <E> entity type
 * @param <T> required field type
 */
public interface RequiredFieldValidator<E extends EntityType<E>, T> extends ChangeValidator {

    /**
     * @return the field that have to be
     */
    EntityField<E, T> requiredField();

    /**
     * @return the error code to return if the field is missing
     */
    String getErrorCode();
}
