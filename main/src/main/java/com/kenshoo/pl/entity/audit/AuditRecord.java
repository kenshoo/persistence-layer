package com.kenshoo.pl.entity.audit;

import com.kenshoo.pl.entity.ChangeOperation;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public class AuditRecord<E extends EntityType<E>> {
    private final E entityType;
    private final String entityId;
    private final Map<? extends EntityField<?, ?>, ?> mandatoryFieldValues;
    private final ChangeOperation operator;
    private final Collection<? extends FieldAuditRecord<E>> fieldRecords;
    private final Collection<? extends AuditRecord<?>> childRecords;

    private AuditRecord(final E entityType,
                        final String entityId,
                        final Map<? extends EntityField<?, ?>, ?> mandatoryFieldValues,
                        final ChangeOperation operator,
                        final Collection<? extends FieldAuditRecord<E>> fieldRecords,
                        final Collection<? extends AuditRecord<?>> childRecords) {
        this.entityType = requireNonNull(entityType, "entityType is required");
        this.entityId = requireNonNull(entityId, "entityId is required");
        this.mandatoryFieldValues = mandatoryFieldValues;
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

    public Collection<? extends Entry<? extends EntityField<? ,?>, ?>> getMandatoryFieldValues() {
        return mandatoryFieldValues.entrySet();
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
        if (maxDepth <= 0) {
            return StringUtils.EMPTY;
        }
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("entityType", entityType.getName())
            .append("entityId", entityId)
            .append("mandatoryFieldValues", mandatoryFieldValues)
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
        private Map<? extends EntityField<?, ?>, ?> mandatoryFieldValues = emptyMap();
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

        public Builder<E> withMandatoryFieldValues(final Map<? extends EntityField<?, ?>, ?> fieldValues) {
            this.mandatoryFieldValues = fieldValues == null ? emptyMap() : fieldValues;
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
                                     mandatoryFieldValues,
                                     operator,
                                     fieldRecords,
                                     childRecords);
        }
    }
}
