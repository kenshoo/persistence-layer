package com.kenshoo.pl.changelog.entity;

import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.Identifier;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.time.Instant;
import java.util.Collection;

public class EntityChangeRecord<E extends EntityType<E>> {
    private final EntityType<E> entityType;
    private final Identifier<E> entityId;
    private final Collection<? extends EntityFieldChangeRecord<E, ?>> fieldChanges;
    private final Collection<? extends EntityChangeRecord<?>> childChanges;
    private final OperationType operationType;
    private final Instant changeTime;

    public EntityChangeRecord(final EntityType<E> entityType,
                              final Identifier<E> entityId,
                              final Collection<? extends EntityFieldChangeRecord<E, ?>> fieldChanges,
                              final Collection<? extends EntityChangeRecord<?>> childChanges,
                              final OperationType operationType,
                              final Instant changeTime) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.fieldChanges = fieldChanges;
        this.childChanges = childChanges;
        this.operationType = operationType;
        this.changeTime = changeTime;
    }

    public EntityType<E> getEntityType() {
        return entityType;
    }

    public Identifier<E> getEntityId() {
        return entityId;
    }

    public Collection<? extends EntityFieldChangeRecord<E, ?>> getFieldChanges() {
        return fieldChanges;
    }

    public Collection<? extends EntityChangeRecord<?>> getChildChanges() {
        return childChanges;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public Instant getChangeTime() {
        return changeTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        @SuppressWarnings("unchecked")
        final EntityChangeRecord<E> that = (EntityChangeRecord<E>) o;

        return new EqualsBuilder()
            .append(entityType, that.entityType)
            .append(entityId, that.entityId)
            .append(fieldChanges, that.fieldChanges)
            .append(childChanges, that.childChanges)
            .append(operationType, that.operationType)
            .append(changeTime, that.changeTime)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(entityType)
            .append(entityId)
            .append(fieldChanges)
            .append(childChanges)
            .append(operationType)
            .append(changeTime)
            .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("entityType", entityType)
            .append("entityId", entityId)
            .append("fieldChanges", fieldChanges)
            .append("childChanges", childChanges)
            .append("operationType", operationType)
            .append("changeTime", changeTime)
            .toString();
    }
}
