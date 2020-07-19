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
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public class AuditedFieldSet<E extends EntityType<E>> {

    private final EntityField<E, ? extends Number> idField;
    // Fields from other entities which are included always in the audit record with their current values
    private final Set<? extends EntityField<?, ?>> externalMandatoryFields;
    // Fields from current entity which are included always in the audit record with their current values
    private final Set<? extends EntityField<E, ?>> selfMandatoryFields;
    // Fields included in the audit record only when changed, with their old and new values (current entityType only)
    private final Set<? extends EntityField<E, ?>> onChangeFields;

    private AuditedFieldSet(final EntityField<E, ? extends Number> idField,
                            final Set<? extends EntityField<?, ?>> externalMandatoryFields,
                            final Set<? extends EntityField<E, ?>> selfMandatoryFields,
                            final Set<? extends EntityField<E, ?>> onChangeFields) {
        this.idField = idField;
        this.externalMandatoryFields = externalMandatoryFields;
        this.selfMandatoryFields = selfMandatoryFields;
        this.onChangeFields = onChangeFields;
    }

    public EntityField<E, ? extends Number> getIdField() {
        return idField;
    }

    public Set<? extends EntityField<?, ?>> getExternalMandatoryFields() {
        return externalMandatoryFields;
    }

    public Set<? extends EntityField<E, ?>> getSelfMandatoryFields() {
        return selfMandatoryFields;
    }

    public Stream<? extends EntityField<?, ?>> getAllMandatoryFields() {
        return Stream.of(externalMandatoryFields, selfMandatoryFields)
                     .flatMap(Set::stream);
    }

    public Stream<? extends EntityField<E, ?>> getAllSelfFields() {
        return Stream.of(selfMandatoryFields, onChangeFields)
                     .flatMap(Set::stream);
    }

    public boolean hasSelfFields() {
        return !selfMandatoryFields.isEmpty() || !onChangeFields.isEmpty();
    }

    public Stream<? extends EntityField<?, ?>> getAllFields() {
        return Stream.of(singleton(idField),
                         externalMandatoryFields,
                         selfMandatoryFields,
                         onChangeFields)
                     .flatMap(Set::stream);
    }

    public AuditedFieldSet<E> intersectWith(final Stream<? extends EntityField<E, ?>> fields) {
        return builder(idField)
            .withExternalMandatoryFields(externalMandatoryFields)
            .withSelfMandatoryFields(selfMandatoryFields)
            .withOnChangeFields(Seq.seq(fields).filter(onChangeFields::contains))
            .build();
    }

    public static <E extends EntityType<E>> Builder<E> builder(final EntityField<E, ? extends Number> idField) {
        return new Builder<>(idField);
    }

    public static class Builder<E extends EntityType<E>> {
        private final EntityField<E, ? extends Number> idField;
        private Set<? extends EntityField<?, ?>> externalMandatoryFields = emptySet();
        private Set<? extends EntityField<E, ?>> selfMandatoryFields = emptySet();
        private Set<? extends EntityField<E, ?>> onChangeFields = emptySet();

        public Builder(EntityField<E, ? extends Number> idField) {
            this.idField = requireNonNull(idField, "idField is required");
        }

        public Builder<E> withExternalMandatoryFields(final EntityField<?, ?>... externalMandatoryFields) {
            this.externalMandatoryFields = externalMandatoryFields == null ? emptySet() : ImmutableSet.copyOf(externalMandatoryFields);
            return this;
        }

        public Builder<E> withExternalMandatoryFields(final Iterable<? extends EntityField<?, ?>> externalMandatoryFields) {
            this.externalMandatoryFields = externalMandatoryFields == null ? emptySet() : ImmutableSet.copyOf(externalMandatoryFields);
            return this;
        }

        @SafeVarargs
        public final Builder<E> withSelfMandatoryFields(final EntityField<E, ?>... selfMandatoryFields) {
            this.selfMandatoryFields = selfMandatoryFields == null ? emptySet() : ImmutableSet.copyOf(selfMandatoryFields);
            return this;
        }

        public Builder<E> withSelfMandatoryFields(final Iterable<? extends EntityField<E, ?>> selfMandatoryFields) {
            this.selfMandatoryFields = selfMandatoryFields == null ? emptySet() : ImmutableSet.copyOf(selfMandatoryFields);
            return this;
        }

        @SafeVarargs
        public final Builder<E> withOnChangeFields(final EntityField<E, ?>... onChangeFields) {
            this.onChangeFields = onChangeFields == null ? emptySet() : ImmutableSet.copyOf(onChangeFields);
            return this;
        }

        public Builder<E> withOnChangeFields(final Iterable<? extends EntityField<E, ?>> onChangeFields) {
            this.onChangeFields = onChangeFields == null ? emptySet() : ImmutableSet.copyOf(onChangeFields);
            return this;
        }

        public AuditedFieldSet<E> build() {
            return new AuditedFieldSet<>(idField,
                                         externalMandatoryFields,
                                         selfMandatoryFields,
                                         onChangeFields);
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
            .append(externalMandatoryFields, that.externalMandatoryFields)
            .append(selfMandatoryFields, that.selfMandatoryFields)
            .append(onChangeFields, that.onChangeFields)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(idField)
            .append(externalMandatoryFields)
            .append(selfMandatoryFields)
            .append(onChangeFields)
            .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
            .append("idField", idField)
            .append("externalMandatoryFields", externalMandatoryFields)
            .append("selfMandatoryFields", selfMandatoryFields)
            .append("onChangeFields", onChangeFields)
            .toString();
    }

}
