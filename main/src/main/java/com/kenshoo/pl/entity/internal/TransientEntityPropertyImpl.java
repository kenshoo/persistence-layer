package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.TransientEntityProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

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
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        TransientEntityPropertyImpl<?, ?> that = (TransientEntityPropertyImpl<?, ?>) o;

        return new EqualsBuilder().append(entityType, that.entityType).append(name, that.name).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(entityType).append(name).toHashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
