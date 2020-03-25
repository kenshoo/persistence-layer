package com.kenshoo.pl.changelog.entity;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class EntityFieldChangeRecord<E extends EntityType<E>, T> {
    private final EntityField<E, T> field;
    private final T oldValue;
    private final T newValue;

    public EntityFieldChangeRecord(final EntityField<E, T> field,
                                   final T oldValue,
                                   final T newValue) {
        this.field = field;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public EntityField<E, T> getField() {
        return field;
    }

    public T getOldValue() {
        return oldValue;
    }

    public T getNewValue() {
        return newValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        @SuppressWarnings("unchecked")
        final EntityFieldChangeRecord<E, T> that = (EntityFieldChangeRecord<E, T>) o;

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
        return new ToStringBuilder(this)
            .append("fieldName", field)
            .append("oldValue", oldValue)
            .append("newValue", newValue)
            .toString();
    }
}
