package com.kenshoo.pl.entity.internal.validators;

import com.kenshoo.pl.entity.*;

import java.util.stream.Stream;

public interface ChangeValidatorAdapter<E extends EntityType<E>> {

    /**
     * @return the validation trigger
     */
    ValidationTrigger<E> trigger();

    /**
     * @return the supported change operation
     */
    SupportedChangeOperation getSupportedChangeOperation();

    /**
     * @return a list of fields to fetch
     */
    Stream<? extends EntityField<?, ?>> fieldsToFetch();

    /**
     * Called by the framework to validate the fields in entity change. The implementation can query the <code>entity</code> only for
     * the fields it has declared in {@link #fieldsToFetch()}.
     *
     * @param entityChange entity change
     * @param currentState existing entity
     *
     * @return a validation error if any, <code>null</code> if none
     */
    ValidationError validate(EntityChange<E> entityChange, CurrentEntityState currentState);
}
