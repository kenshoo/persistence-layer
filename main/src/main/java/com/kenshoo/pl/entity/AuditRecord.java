package com.kenshoo.pl.entity;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Collection;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

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
        this.entityType = requireNonNull(entityType, "entityType is required");
        this.entityId = requireNonNull(entityId, "entityId is required");
        this.operator = requireNonNull(operator, "operator is required");
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

    public boolean hasFieldRecord(final FieldAuditRecord<E> fieldRecord) {
        return fieldRecords.contains(fieldRecord);
    }

    public boolean hasFieldRecordFor(final EntityField<E, ?> field) {
        return fieldRecords.stream()
                           .anyMatch(fieldRecord -> fieldRecord.getField().equals(field));
    }

    public Collection<? extends AuditRecord<?>> getChildRecords() {
        return childRecords;
    }

    public boolean hasSameChildRecord(final AuditRecord<?> childRecord) {
        return childRecords.contains(childRecord);
    }

    public boolean hasNoChanges() {
        return fieldRecords.isEmpty() && childRecords.isEmpty();
    }

    /**
     * Generates a deep string representation of the entire hierarchy of records.<br>
     * WARNING: if there are many nested levels of child records, will have poor performance!
     */
    @Override
    public String toString() {
        return toString(Integer.MAX_VALUE);
    }

    /**
     * Generates a deep string representation limited to the given number of nested levels.
     * @param maxDepth maximum depth of recursion, must be at least one (one means without child records).
     */
    public String toString(final int maxDepth) {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("entityType", entityType.getName())
            .append("entityId", entityId)
            .append("operator", operator)
            .append("fieldRecords", fieldRecords)
            .append("childRecords", childRecordsToString(maxDepth))
            .toString();
    }

    private String childRecordsToString(final int maxDepth) {
        return childRecords.stream()
            .map(childRecord -> childRecord.toString(maxDepth - 1))
            .collect(toList())
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
