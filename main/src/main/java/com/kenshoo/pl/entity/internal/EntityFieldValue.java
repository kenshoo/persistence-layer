package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.EntityField;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class EntityFieldValue {
    private final EntityField<?, ?> entityField;
    private final Object value;

    public EntityFieldValue(final EntityField<?, ?> entityField, final Object value) {
        this.entityField = entityField;
        this.value = value;
    }

    public EntityField<?, ?> getField() {
        return entityField;
    }

    public Object getValue() {
        return value;
    }


    public String getFieldName() {
        return entityField.getEntityType().getName() + "." + entityField;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        EntityFieldValue that = (EntityFieldValue) o;

        return new EqualsBuilder()
            .append(entityField, that.entityField)
            .append(value, that.value)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(entityField)
            .append(value)
            .toHashCode();
    }

    @Override
    public String toString() {
        return getFieldName() + "=" + value;
    }
}
