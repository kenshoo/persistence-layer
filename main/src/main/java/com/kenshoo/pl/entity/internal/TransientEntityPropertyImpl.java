package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.TransientEntityProperty;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.Validate.notBlank;

public class TransientEntityPropertyImpl<E extends EntityType<E>, T> implements TransientEntityProperty<E, T> {

    private final EntityType<E> entityType;
    private final String name;

    public TransientEntityPropertyImpl(final EntityType<E> entityType, String name) {
        this.entityType = requireNonNull(entityType, "An entity type is required");
        this.name = notBlank(name, "A non-blank name is required");
    }

    @Override
    public EntityType<E> getEntityType() {
        return entityType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
