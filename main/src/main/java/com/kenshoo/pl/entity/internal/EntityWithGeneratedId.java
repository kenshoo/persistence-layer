package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.Entity;
import com.kenshoo.pl.entity.EntityField;

public class EntityWithGeneratedId implements Entity {

    private final Entity sharedData;
    private final EntityField<?, Object> idField;
    private final Object idValue;

    public EntityWithGeneratedId(Entity sharedData, EntityField<?, Object> idField, Object idValue) {
        this.sharedData = sharedData;
        this.idField = idField;
        this.idValue = idValue;
    }

    @Override
    public boolean containsField(EntityField<?, ?> field) {
        return field == idField || sharedData.containsField(field);
    }

    @Override
    public <T> T get(EntityField<?, T> field) {
        return field == idField
                ? (T)idValue
                : sharedData.get(field);
    }

}
