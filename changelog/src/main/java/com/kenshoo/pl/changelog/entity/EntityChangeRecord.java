package com.kenshoo.pl.changelog.entity;

import com.kenshoo.pl.entity.ChangeOperation;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.Identifier;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Collection;

public class EntityChangeRecord<E extends EntityType<E>> {
    private final E entityType;
    private final Identifier<E> entityId;
    private final Collection<? extends EntityFieldChangeRecord<E>> fieldChanges;
    private final Collection<? extends EntityChangeRecord<?>> childChanges;
    private final ChangeOperation operation;
    private final String actionId;

    public EntityChangeRecord(final E entityType,
                              final Identifier<E> entityId,
                              final Collection<? extends EntityFieldChangeRecord<E>> fieldChanges,
                              final Collection<? extends EntityChangeRecord<?>> childChanges,
                              final ChangeOperation operation,
                              final String actionId) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.fieldChanges = fieldChanges;
        this.childChanges = childChanges;
        this.operation = operation;
        this.actionId = actionId;
    }

    public E getEntityType() {
        return entityType;
    }

    public Identifier<E> getEntityId() {
        return entityId;
    }

    public Collection<? extends EntityFieldChangeRecord<E>> getFieldChanges() {
        return fieldChanges;
    }

    public Collection<? extends EntityChangeRecord<?>> getChildChanges() {
        return childChanges;
    }

    public ChangeOperation getOperation() {
        return operation;
    }

    public String getActionId() {
        return actionId;
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
            .append(childChanges, that.childChanges)
            .append(operation, that.operation)
            .append(actionId, that.actionId)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(entityType)
            .append(entityId)
            .append(fieldChanges)
            .append(childChanges)
            .append(operation)
            .append(actionId)
            .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("entityType", entityType)
            .append("entityId", entityId)
            .append("fieldChanges", fieldChanges)
            .append("childChanges", childChanges)
            .append("operation", operation)
            .append("actionId", actionId)
            .toString();
    }


    public static class Builder<E extends EntityType<E>> {
        private E entityType;
        private Identifier<E> entityId;
        private Collection<? extends EntityFieldChangeRecord<E>> fieldChanges;
        private Collection<? extends EntityChangeRecord<?>> childChanges;
        private ChangeOperation operation;
        private String actionId;

        public Builder<E> withEntityType(E entityType) {
            this.entityType = entityType;
            return this;
        }

        public Builder<E> withEntityId(Identifier<E> entityId) {
            this.entityId = entityId;
            return this;
        }

        public Builder<E> withFieldChanges(Collection<? extends EntityFieldChangeRecord<E>> fieldChanges) {
            this.fieldChanges = fieldChanges;
            return this;
        }

        public Builder<E> withChildChanges(Collection<? extends EntityChangeRecord<?>> childChanges) {
            this.childChanges = childChanges;
            return this;
        }

        public Builder<E> withOperation(ChangeOperation operation) {
            this.operation = operation;
            return this;
        }

        public Builder<E> withActionId(String actionId) {
            this.actionId = actionId;
            return this;
        }

        public EntityChangeRecord<E> build() {
            return new EntityChangeRecord<>(entityType,
                                            entityId,
                                            fieldChanges,
                                            childChanges,
                                            operation,
                                            actionId);
        }
    }
}
