package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.pl.entity.ChangeOperation;
import com.kenshoo.pl.entity.EntityType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Collection;

import static java.util.Collections.emptySet;

public class AuditRecord<E extends EntityType<E>> {
    private final E entityType;
    private final Number entityId;
    private final ChangeOperation operation;
    private final Collection<? extends FieldAuditRecord<E>> fieldRecords;
    private final Collection<? extends AuditRecord<?>> childRecords;

    public AuditRecord(final E entityType,
                       final Number entityId,
                       final ChangeOperation operation,
                       final Collection<? extends FieldAuditRecord<E>> fieldRecords,
                       final Collection<? extends AuditRecord<?>> childRecords) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.operation = operation;
        this.fieldRecords = fieldRecords;
        this.childRecords = childRecords;
    }

    public E getEntityType() {
        return entityType;
    }

    public Number getEntityId() {
        return entityId;
    }

    public ChangeOperation getOperation() {
        return operation;
    }

    public Collection<? extends FieldAuditRecord<E>> getFieldRecords() {
        return fieldRecords;
    }

    public Collection<? extends AuditRecord<?>> getChildRecords() {
        return childRecords;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        AuditRecord<?> that = (AuditRecord<?>) o;

        return new EqualsBuilder()
            .append(entityType, that.entityType)
            .append(entityId, that.entityId)
            .append(operation, that.operation)
            .append(fieldRecords, that.fieldRecords)
            .append(childRecords, that.childRecords)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(entityType)
            .append(entityId)
            .append(operation)
            .append(fieldRecords)
            .append(childRecords)
            .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("entityType", entityType)
            .append("entityId", entityId)
            .append("operation", operation)
            .append("fieldChanges", fieldRecords)
            .append("childChanges", childRecords)
            .toString();
    }


    public static class Builder<E extends EntityType<E>> {
        private E entityType;
        private Number entityId;
        private ChangeOperation operation;
        private Collection<? extends FieldAuditRecord<E>> fieldRecords = emptySet();
        private Collection<? extends AuditRecord<?>> childRecord = emptySet();

        public Builder<E> withEntityType(E entityType) {
            this.entityType = entityType;
            return this;
        }

        public Builder<E> withEntityId(Number entityId) {
            this.entityId = entityId;
            return this;
        }

        public Builder<E> withOperation(ChangeOperation operation) {
            this.operation = operation;
            return this;
        }

        public Builder<E> withFieldChanges(Collection<? extends FieldAuditRecord<E>> fieldChanges) {
            this.fieldRecords = fieldChanges;
            return this;
        }

        public Builder<E> withChildChanges(Collection<? extends AuditRecord<?>> childChanges) {
            this.childRecord = childChanges;
            return this;
        }

        public AuditRecord<E> build() {
            return new AuditRecord<>(entityType,
                                     entityId,
                                     operation,
                                     fieldRecords,
                                     childRecord);
        }
    }
}
