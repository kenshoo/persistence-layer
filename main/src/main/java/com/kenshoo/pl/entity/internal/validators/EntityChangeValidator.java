package com.kenshoo.pl.entity.internal.validators;

import com.kenshoo.pl.entity.Entity;
import com.kenshoo.pl.entity.EntityChange;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.SupportedChangeOperation;
import com.kenshoo.pl.entity.ValidationError;

import java.util.stream.Stream;

/**
 * A validator that checks number of fields and uses entity and parent fields for the verification.
 *
 * @param <E> entity type
 */
public interface EntityChangeValidator<E extends EntityType<E>> {

    /**
     * @return the field validated by this validator
     */
    Stream<EntityField<E, ?>> validatedFields();

    /**
     * @return the supported change operation
     */
    SupportedChangeOperation getSupportedChangeOperation();

    /**
     * @return a list of fields to fetch
     */
    Stream<? extends EntityField<?, ?>> fetchFields();

    /**
     * Called by the framework to validate the fields in entity change. The implementation can query the <code>entity</code> only for
     * the fields it has declared in {@link #fetchFields()}.
     *
     * @param entityChange entity change
     * @param currentState existing entity
     *
     * @return a validation error if any, <code>null</code> if none
     */
    ValidationError validate(EntityChange<E> entityChange, Entity currentState);

}
