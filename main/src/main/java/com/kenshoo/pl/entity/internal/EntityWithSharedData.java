package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.Entity;
import com.kenshoo.pl.entity.EntityField;

public class EntityWithSharedData extends EntityImpl {

    private final Entity sharedData;

    public EntityWithSharedData(Entity sharedData) {
        this.sharedData = sharedData;
    }

    public boolean containsField(EntityField<?, ?> field) {
        return sharedData.containsField(field) || super.containsField(field);
    }

    @Override
    public <T> T get(EntityField<?, T> field) {
        return this.containsField(field)
                ? super.get(field)
                : sharedData.get(field);
    }

    public <T> void set(EntityField<?, T> field, T value) {
        super.set(field, value);
    }

}
