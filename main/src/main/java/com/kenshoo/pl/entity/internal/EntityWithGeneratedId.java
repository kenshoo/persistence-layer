package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.CurrentEntityState;
import com.kenshoo.pl.entity.EntityField;

public class EntityWithGeneratedId extends CurrentEntityState {

    private final EntityField<?, Object> idField;
    private final Object idValue;

    public EntityWithGeneratedId(EntityField<?, Object> idField, Object idValue) {
        this.idField = idField;
        this.idValue = idValue;
    }

    @Override
    public boolean containsField(EntityField<?, ?> field) {
        return field == idField;
    }

    @Override
    public <T> T get(EntityField<?, T> field) {
        if (idValue == null) {
            throw new IllegalArgumentException("Field " + field + " of entity \"" + field.getEntityType().getName() + "\" is not fetched");
        }
        //noinspection unchecked
        return (T) idValue;

    }

}
