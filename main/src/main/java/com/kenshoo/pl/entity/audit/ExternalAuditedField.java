package com.kenshoo.pl.entity.audit;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import static java.util.Objects.requireNonNull;

public class ExternalAuditedField<E extends EntityType<E>, T> {

    private final EntityField<E, T> field;
    private final String name;

    public ExternalAuditedField(final EntityField<E, T> field) {
        this(requireNonNull(field, "An underlying field must be provided"),
             field.toString());
    }

    public ExternalAuditedField(final EntityField<E, T> field,
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ExternalAuditedField<?, ?> that = (ExternalAuditedField<?, ?>) o;

        return new EqualsBuilder().append(field, that.field).append(name, that.name).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(field).append(name).toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("field", field)
            .append("name", name)
            .toString();
    }
}
