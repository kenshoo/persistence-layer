package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.pl.entity.Entity;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.Triptional;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import static java.util.Objects.requireNonNull;

public class AuditedField<E extends EntityType<E>, T> {

    private final EntityField<E, T> field;
    private final String name;

    public AuditedField(final EntityField<E, T> field) {
        this(requireNonNull(field, "A field must be provided"),
             field.toString());
    }

    public AuditedField(final EntityField<E, T> field,
                        final String name) {
        this.field = requireNonNull(field, "An underlying field must be provided");
        this.name = requireNonNull(name, "A name must be provided");
    }

    public EntityField<E, T> getField() {
        return field;
    }

    public String getName() {
        return name;
    }

    public Triptional<T> getValue(final Entity entity) {
        return entity.safeGet(field);
    }

    public boolean valuesEqual(T v1, T v2) {
        return field.valuesEqual(v1, v2);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        AuditedField<?, ?> that = (AuditedField<?, ?>) o;

        return new EqualsBuilder()
            .append(field, that.field)
            .append(name, that.name)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(field)
            .append(name)
            .toHashCode();
    }
}
