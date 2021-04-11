package com.kenshoo.pl.entity.audit;

import com.kenshoo.pl.entity.ChangeOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Collection;
import java.util.Map.Entry;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public class AuditRecord {
    private final String entityType;
    private final String entityId;
    private final Collection<? extends Entry<String, ?>> mandatoryFieldValues;
    private final ChangeOperation operator;
    private final Collection<? extends FieldAuditRecord> fieldRecords;
    private final Collection<? extends AuditRecord> childRecords;

    private AuditRecord(final String entityType,
                        final String entityId,
                        final Collection<? extends Entry<String, ?>> mandatoryFieldValues,
                        final ChangeOperation operator,
                        final Collection<? extends FieldAuditRecord> fieldRecords,
                        final Collection<? extends AuditRecord> childRecords) {
        this.entityType = requireNonNull(entityType, "entityType is required");
        this.entityId = requireNonNull(entityId, "entityId is required");
        this.mandatoryFieldValues = mandatoryFieldValues;
        this.operator = requireNonNull(operator, "operator is required");
        this.fieldRecords = fieldRecords;
        this.childRecords = childRecords;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public Collection<? extends Entry<String, ?>> getMandatoryFieldValues() {
        return mandatoryFieldValues;
    }

    public ChangeOperation getOperator() {
        return operator;
    }

    public Collection<? extends FieldAuditRecord> getFieldRecords() {
        return fieldRecords;
    }

    public Collection<? extends AuditRecord> getChildRecords() {
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
            .append("entityType", entityType)
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

    public static class Builder {
        private String entityType;
        private String entityId;
        private Collection<? extends Entry<String, ?>> mandatoryFieldValues = emptyList();
        private ChangeOperation operator;
        private Collection<? extends FieldAuditRecord> fieldRecords = emptyList();
        private Collection<? extends AuditRecord> childRecords = emptyList();

        public Builder withEntityType(String entityType) {
            this.entityType = entityType;
            return this;
        }

        public Builder withEntityId(String entityId) {
            this.entityId = entityId;
            return this;
        }

        public Builder withOperator(ChangeOperation operator) {
            this.operator = operator;
            return this;
        }

        public Builder withMandatoryFieldValues(final Collection<? extends Entry<String, ?>> fieldValues) {
            this.mandatoryFieldValues = fieldValues == null ? emptyList() : fieldValues;
            return this;
        }

        public Builder withFieldRecords(Collection<? extends FieldAuditRecord> fieldRecords) {
            this.fieldRecords = fieldRecords == null ? emptyList() : fieldRecords;
            return this;
        }

        public Builder withChildRecords(Collection<? extends AuditRecord> childRecords) {
            this.childRecords = childRecords == null ? emptyList() : childRecords;
            return this;
        }

        public AuditRecord build() {
            return new AuditRecord(entityType,
                                   entityId,
                                   mandatoryFieldValues,
                                   operator,
                                   fieldRecords,
                                   childRecords);
        }
    }
}
