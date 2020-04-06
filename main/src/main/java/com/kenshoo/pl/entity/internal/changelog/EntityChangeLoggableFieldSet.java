package com.kenshoo.pl.entity.internal.changelog;

import com.google.common.collect.ImmutableSet;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

public class EntityChangeLoggableFieldSet<E extends EntityType<E>> {

    private final EntityField<E, ? extends Number> idField;
    private final Set<? extends EntityField<E, ?>> additionalFields;

    public EntityChangeLoggableFieldSet(final EntityField<E, ? extends Number> idField,
                                        final Collection<? extends EntityField<E, ?>> additionalFields) {
        this.idField = requireNonNull(idField, "idField is required");
        this.additionalFields = additionalFields == null ? emptySet() : ImmutableSet.copyOf(additionalFields);
    }

    public EntityField<E, ? extends Number> getIdField() {
        return idField;
    }

    public Set<? extends EntityField<E, ?>> getAdditionalFields() {
        return additionalFields;
    }

    public Set<? extends EntityField<E, ?>> getAllFields() {
        return Stream.concat(Stream.of(idField),
                             additionalFields.stream())
                     .collect(toSet());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final EntityChangeLoggableFieldSet<?> that = (EntityChangeLoggableFieldSet<?>) o;

        return new EqualsBuilder()
            .append(idField, that.idField)
            .append(additionalFields, that.additionalFields)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(idField)
            .append(additionalFields)
            .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("idField", idField)
            .append("additionalFields", additionalFields)
            .toString();
    }

}
