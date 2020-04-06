package com.kenshoo.pl.entity.internal.changelog;

import com.kenshoo.pl.entity.ChangeOperation;
import com.kenshoo.pl.entity.EntityType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Collection;

import static java.util.Collections.emptySet;

public class EntityChangeRecord<E extends EntityType<E>> {
    private final E entityType;
    private final Number entityId;
    private final Collection<? extends EntityFieldChangeRecord<E>> fieldChanges;
    private final ChangeOperation operation;

    public EntityChangeRecord(final E entityType,
                              final Number entityId,
                              final Collection<? extends EntityFieldChangeRecord<E>> fieldChanges,
                              final ChangeOperation operation) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.fieldChanges = fieldChanges;
        this.operation = operation;
    }

    public E getEntityType() {
        return entityType;
    }

    public Number getEntityId() {
        return entityId;
    }

    public Collection<? extends EntityFieldChangeRecord<E>> getFieldChanges() {
        return fieldChanges;
    }

    public ChangeOperation getOperation() {
        return operation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        EntityChangeRecord<?> that = (EntityChangeRecord<?>) o;

        return new EqualsBuilder()
            .append(entityType, that.entityType)
            .append(entityId, that.entityId)
            .append(fieldChanges, that.fieldChanges)
            .append(operation, that.operation)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(entityType)
            .append(entityId)
            .append(fieldChanges)
            .append(operation)
            .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("entityType", entityType)
            .append("entityId", entityId)
            .append("fieldChanges", fieldChanges)
            .append("operation", operation)
            .toString();
    }


    public static class Builder<E extends EntityType<E>> {
        private E entityType;
        private Number entityId;
        private Collection<? extends EntityFieldChangeRecord<E>> fieldChanges = emptySet();
        private ChangeOperation operation;

        public Builder<E> withEntityType(E entityType) {
            this.entityType = entityType;
            return this;
        }

        public Builder<E> withEntityId(Number entityId) {
            this.entityId = entityId;
            return this;
        }

        public Builder<E> withFieldChanges(Collection<? extends EntityFieldChangeRecord<E>> fieldChanges) {
            this.fieldChanges = fieldChanges;
            return this;
        }

        public Builder<E> withOperation(ChangeOperation operation) {
            this.operation = operation;
            return this;
        }

        public EntityChangeRecord<E> build() {
            return new EntityChangeRecord<>(entityType,
                                            entityId,
                                            fieldChanges,
                                            operation
            );
        }
    }
}
