package com.kenshoo.pl.entity.audit;

import com.kenshoo.pl.entity.EntityField;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class FieldAuditRecord {
    private final String field;
    private final Object oldValue;
    private final Object newValue;

    private FieldAuditRecord(final String field,
                             final Object oldValue,
                             final Object newValue) {
        this.field = field;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public String getField() {
        return field;
    }

    public Optional<?> getOldValue() {
        return Optional.ofNullable(oldValue);
    }

    public Optional<?> getNewValue() {
        return Optional.ofNullable(newValue);
    }

    public static Builder builder(final EntityField<?, ?> field) {
        return new Builder(field.toString());
    }

    public static Builder builder(final String field) {
        return new Builder(field);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final FieldAuditRecord that = (FieldAuditRecord) o;

        return new EqualsBuilder()
            .append(field, that.field)
            .append(oldValue, that.oldValue)
            .append(newValue, that.newValue)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(field)
            .append(oldValue)
            .append(newValue)
            .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("field", field)
            .append("oldValue", oldValue)
            .append("newValue", newValue)
            .toString();
    }

    public static class Builder {
        private final String field;
        private Object oldValue;
        private Object newValue;

        private Builder(final String field) {
            this.field = requireNonNull(field, "A field is required");
        }

        public Builder oldValue(Object oldValue) {
            this.oldValue = oldValue;
            return this;
        }

        public Builder newValue(Object newValue) {
            this.newValue = newValue;
            return this;
        }

        public FieldAuditRecord build() {
            return new FieldAuditRecord(field, oldValue, newValue);
        }
    }
}
