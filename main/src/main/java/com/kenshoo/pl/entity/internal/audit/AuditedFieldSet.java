package com.kenshoo.pl.entity.internal.audit;

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

public class AuditedFieldSet<E extends EntityType<E>> {

    private final EntityField<E, ? extends Number> idField;
    private final Set<? extends EntityField<E, ?>> dataFields;

    public AuditedFieldSet(final EntityField<E, ? extends Number> idField) {
        this(idField, emptySet());
    }
    public AuditedFieldSet(final EntityField<E, ? extends Number> idField,
                           final Collection<? extends EntityField<E, ?>> dataFields) {
        this.idField = requireNonNull(idField, "idField is required");
        this.dataFields = dataFields == null ? emptySet() : ImmutableSet.copyOf(dataFields);
    }

    public EntityField<E, ? extends Number> getIdField() {
        return idField;
    }

    public Set<? extends EntityField<E, ?>> getDataFields() {
        return dataFields;
    }

    public Set<? extends EntityField<E, ?>> getAllFields() {
        return Stream.concat(Stream.of(idField),
                             dataFields.stream())
                     .collect(toSet());
    }

    public AuditedFieldSet<E> intersectWith(final Collection<? extends EntityField<E, ?>> fields) {
        return new AuditedFieldSet<>(idField,
                                     fields.stream()
                                           .filter(dataFields::contains)
                                           .collect(toSet()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final AuditedFieldSet<?> that = (AuditedFieldSet<?>) o;

        return new EqualsBuilder()
            .append(idField, that.idField)
            .append(dataFields, that.dataFields)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(idField)
            .append(dataFields)
            .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("idField", idField)
            .append("dataFields", dataFields)
            .toString();
    }

}
