package com.kenshoo.pl.entity.spi;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.ValidationError;

/**
 * A validator that checks something that relates to the state of one of the parent entities, not the entity itself.
 * For example, a security validator checks that the current user is allowed to access the profile this entity belongs to.
 *
 * @param <T> entity type
 */
public interface ParentConditionValidator<T> {

    /**
     * @return the field of one of the parent entities whose value is required for the validation
     */
    EntityField<?, T> parentIdField();

    /**
     * Validates that the modification or creation of a child entity is allowed for the parent entity specified.
     *
     * @param parentId the value of the field returned by {@link #parentIdField()}
     * @return validation error or null if allowed
     */
    ValidationError validate(T parentId);

}
