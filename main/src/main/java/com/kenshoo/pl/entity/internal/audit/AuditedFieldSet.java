package com.kenshoo.pl.entity.internal.audit;

import com.google.common.collect.ImmutableSet;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jooq.lambda.Seq;

import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;

public class AuditedFieldSet<E extends EntityType<E>> {

    private final EntityField<E, ? extends Number> idField;
    private final Set<? extends EntityField<?, ?>> ancestorFields;
    private final Set<? extends EntityField<E, ?>> dataFields;

    private AuditedFieldSet(final EntityField<E, ? extends Number> idField,
                            final Set<? extends EntityField<?, ?>> ancestorFields,
                            final Set<? extends EntityField<E, ?>> dataFields) {
        this.idField = idField;
        this.ancestorFields = ancestorFields;
        this.dataFields = dataFields;
    }

    public EntityField<E, ? extends Number> getIdField() {
        return idField;
    }

    public Set<? extends EntityField<?, ?>> getAncestorFields() {
        return ancestorFields;
    }

    public Set<? extends EntityField<E, ?>> getDataFields() {
        return dataFields;
    }

    public Stream<? extends EntityField<?, ?>> getAllFields() {
        return Stream.of(singleton(idField),
                         ancestorFields,
                         dataFields)
                     .flatMap(Set::stream);
    }

    public AuditedFieldSet<E> intersectWith(final Stream<? extends EntityField<E, ?>> fields) {
        return builder(idField)
            .withAncestorFields(ancestorFields)
            .withDataFields(Seq.seq(fields).filter(dataFields::contains))
            .build();
    }

    public static <E extends EntityType<E>> Builder<E> builder(final EntityField<E, ? extends Number> idField) {
        return new Builder<>(idField);
    }

    public static class Builder<E extends EntityType<E>> {
        private final EntityField<E, ? extends Number> idField;
        private Set<? extends EntityField<?, ?>> ancestorFields = emptySet();
        private Set<? extends EntityField<E, ?>> dataFields = emptySet();

        public Builder(EntityField<E, ? extends Number> idField) {
            this.idField = requireNonNull(idField, "idField is required");
        }

        public Builder<E> withAncestorFields(final Iterable<? extends EntityField<?, ?>> ancestorFields) {
            this.ancestorFields = ancestorFields == null ? emptySet() : ImmutableSet.copyOf(ancestorFields);
            return this;
        }

        public Builder<E> withDataFields(final Iterable<? extends EntityField<E, ?>> dataFields) {
            this.dataFields = dataFields == null ? emptySet() : ImmutableSet.copyOf(dataFields);
            return this;
        }

        public AuditedFieldSet<E> build() {
            return new AuditedFieldSet<>(idField, ancestorFields, dataFields);
        }
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
            .append(ancestorFields, that.ancestorFields)
            .append(dataFields, that.dataFields)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(idField)
            .append(ancestorFields)
            .append(dataFields)
            .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("idField", idField)
            .append("ancestorFields", ancestorFields)
            .append("dataFields", dataFields)
            .toString();
    }

}
