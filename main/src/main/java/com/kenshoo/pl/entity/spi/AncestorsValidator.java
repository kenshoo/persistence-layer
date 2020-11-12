package com.kenshoo.pl.entity.spi;

import com.kenshoo.pl.entity.*;

import java.util.stream.Stream;

public interface AncestorsValidator extends ChangeValidator {

    /**
     * @return the fields of the ancestor entities whose value is required for the validation
     */
    Stream<EntityField<?, ?>> ancestorsFields();

    /**
     * Called by the framework to check if the change is restricted by ancestors. The implementation can query the <code>entity</code> only for
     * the fields it has declared in {@link #ancestorsFields()}.
     *
     * @param ancestorsFieldsState ancestor's fields
     *
     * @return a validation error if any, <code>null</code> if none
     */
    ValidationError validate(CurrentEntityState ancestorsFieldsState);

}
