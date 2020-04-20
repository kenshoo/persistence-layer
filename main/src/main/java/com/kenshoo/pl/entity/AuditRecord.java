package com.kenshoo.pl.entity;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Collection;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;

public class AuditRecord<E extends EntityType<E>> {
    private final E entityType;
    private final String entityId;
    private final ChangeOperation operator;
    private final Collection<? extends FieldAuditRecord<E>> fieldRecords;
    private final Collection<? extends AuditRecord<?>> childRecords;

    private AuditRecord(final E entityType,
                       final String entityId,
                       final ChangeOperation operator,
                       final Collection<? extends FieldAuditRecord<E>> fieldRecords,
                       final Collection<? extends AuditRecord<?>> childRecords) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.operator = operator;
        this.fieldRecords = fieldRecords;
        this.childRecords = childRecords;
    }

    public E getEntityType() {
        return entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public ChangeOperation getOperator() {
        return operator;
    }

    public Collection<? extends FieldAuditRecord<E>> getFieldRecords() {
        return fieldRecords;
    }

    public Collection<? extends AuditRecord<?>> getChildRecords() {
        return childRecords;
    }

    public boolean hasNoChanges() {
        return fieldRecords.isEmpty() && childRecords.isEmpty();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("entityType", entityType)
            .append("entityId", entityId)
            .append("operator", operator)
            .append("fieldRecords", fieldRecords)
            .append("childRecords", childRecords)
            .toString();
    }

    public static class Builder<E extends EntityType<E>> {
        private E entityType;
        private String entityId;
        private ChangeOperation operator;
        private Collection<? extends FieldAuditRecord<E>> fieldRecords = emptyList();
        private Collection<? extends AuditRecord<?>> childRecords = emptyList();

        public Builder<E> withEntityType(E entityType) {
            this.entityType = entityType;
            return this;
        }

        public Builder<E> withEntityId(String entityId) {
            this.entityId = entityId;
            return this;
        }

        public Builder<E> withOperator(ChangeOperation operator) {
            this.operator = operator;
            return this;
        }

        public Builder<E> withFieldRecords(Collection<? extends FieldAuditRecord<E>> fieldRecords) {
            this.fieldRecords = fieldRecords == null ? emptyList() : fieldRecords;
            return this;
        }

        public Builder<E> withChildRecords(Collection<? extends AuditRecord<?>> childRecords) {
            this.childRecords = childRecords == null ? emptyList() : childRecords;
            return this;
        }

        public AuditRecord<E> build() {
            return new AuditRecord<>(entityType,
                                     entityId,
                                     operator,
                                     fieldRecords,
                                     childRecords);
        }
    }
}
