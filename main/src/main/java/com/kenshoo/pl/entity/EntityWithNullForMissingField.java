package com.kenshoo.pl.entity;

public class EntityWithNullForMissingField implements Entity {

    private final Entity entity;

    public EntityWithNullForMissingField(Entity entity) {
        this.entity = entity;
    }

    @Override
    public boolean containsField(EntityField<?, ?> field) {
        return entity.containsField(field);
    }

    @Override
    public <T> T get(EntityField<?, T> field) {
        return containsField(field) ? entity.get(field) : null;
    }
}
