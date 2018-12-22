package com.kenshoo.pl.entity;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;

public class ChangeContext {

    private final Multimap<EntityChange, ValidationError> validationErrors = HashMultimap.create();
    private final Map<EntityChange, Entity> entities = new IdentityHashMap<>();
    private final PersistentLayerStats stats = new PersistentLayerStats();

    public Entity getEntity(EntityChange entityChange) {
        return entities.get(entityChange);
    }

    public void addEntity(EntityChange change, Entity entity) {
        this.entities.put(change, entity);
    }

    public void addValidationError(EntityChange<? extends EntityType<?>> entityChange, ValidationError error) {
        validationErrors.put(entityChange, error);
    }

    public boolean hasValidationErrors() {
        return !validationErrors.isEmpty();
    }

    public Collection<ValidationError> getValidationErrors(EntityChange entityChange) {
        return validationErrors.get(entityChange);
    }

    public boolean containsError(EntityChange entityChange) {
        return validationErrors.containsKey(entityChange);
    }

    public PersistentLayerStats getStats() {
        return stats;
    }
}
