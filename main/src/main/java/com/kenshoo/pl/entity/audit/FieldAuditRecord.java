package com.kenshoo.pl.entity.audit;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import static java.util.Objects.requireNonNull;

public class FieldAuditRecord<E extends EntityType<E>> {
    private final EntityField<E, ?> field;
    private final Object oldValue;
    private final Object newValue;

    public FieldAuditRecord(final EntityField<E, ?> field,
                            final Object oldValue,
                            final Object newValue) {
        this.field = requireNonNull(field, "A field is required");
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public EntityField<E, ?> getField() {
        return field;
    }

    public Object getOldValue() {
        return oldValue;
    }

    public Object getNewValue() {
        return newValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        @SuppressWarnings("unchecked")
        final FieldAuditRecord<E> that = (FieldAuditRecord<E>) o;

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
            .append("field", field.getEntityType().getName() + "." + field)
            .append("oldValue", oldValue)
            .append("newValue", newValue)
            .toString();
    }
}
