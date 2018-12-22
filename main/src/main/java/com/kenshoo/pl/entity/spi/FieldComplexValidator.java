package com.kenshoo.pl.entity.spi;

import com.kenshoo.pl.entity.Entity;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.ValidationError;

import java.util.stream.Stream;

/**
 * A validator that checks one field and uses parent entity fields for the verification. For instance,
 * a validator that checks that a bid doesn't exceed campaign budget, would implement this interface.
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
     * @param entity existing entity
     *
     * @return a validation error if any, <code>null</code> if none
     */
    ValidationError validate(T fieldValue, Entity entity);

    /**
     * @return a list of fields to fetch. Can contain only parent entities fields.
     */
    Stream<EntityField<?, ?>> fetchFields();
}
