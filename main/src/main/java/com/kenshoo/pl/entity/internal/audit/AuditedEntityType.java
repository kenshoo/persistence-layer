package com.kenshoo.pl.entity.internal.audit;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.audit.AuditTrigger;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static com.kenshoo.pl.entity.audit.AuditTrigger.*;
import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public class AuditedEntityType<E extends EntityType<E>> {

    private final EntityField<E, ? extends Number> idField;

    private final String name;

    // Fields from other entities which are included always in the audit record with their current values
    private final Set<? extends AuditedField<?, ?>> externalFields;

    // Fields from current entity (besides id), keyed by audit trigger type
    private final SetMultimap<AuditTrigger, ? extends AuditedField<E, ?>> internalFields;

    private AuditedEntityType(final EntityField<E, ? extends Number> idField,
                              final String name,
                              final Set<? extends AuditedField<?, ?>> externalFields,
                              final SetMultimap<AuditTrigger, ? extends AuditedField<E, ?>> internalFields) {
        this.idField = idField;
        this.name = name;
        this.externalFields = externalFields;
        this.internalFields = internalFields;
    }

    public EntityField<E, ? extends Number> getIdField() {
        return idField;
    }

    public String getName() {
        return name;
    }

    public Set<? extends AuditedField<?, ?>> getExternalFields() {
        return externalFields;
    }

    public Stream<? extends EntityField<?, ?>> getUnderlyingMandatoryFields() {
        return getMandatoryFields().map(AuditedField::getField);
    }

    public Stream<? extends AuditedField<?, ?>> getMandatoryFields() {
        return Stream.of(externalFields, internalFields.get(ALWAYS))
                     .flatMap(Set::stream);
    }

    public Stream<? extends EntityField<E, ?>> getUnderlyingOnChangeFields() {
        return Stream.of(ON_CREATE_OR_UPDATE, ON_UPDATE)
                     .flatMap(trigger -> internalFields.get(trigger).stream())
                     .map(AuditedField::getField);
    }

    public Stream<? extends AuditedField<E, ?>> getInternalFields() {
        return internalFields.values().stream();
    }

    public boolean hasInternalFields() {
        return !internalFields.isEmpty();
    }

    public static <E extends EntityType<E>> Builder<E> builder(final EntityField<E, ? extends Number> idField) {
        return new Builder<>(idField);
    }

    public static class Builder<E extends EntityType<E>> {
        private final EntityField<E, ? extends Number> idField;
        private String name;
        private Set<? extends AuditedField<?, ?>> externalFields = emptySet();
        private final SetMultimap<AuditTrigger, AuditedField<E, ?>> internalFields = HashMultimap.create();

        public Builder(final EntityField<E, ? extends Number> idField) {
            this.idField = requireNonNull(idField, "idField is required");
            this.name = idField.getEntityType().getName();
            Stream.of(ALWAYS, ON_CREATE_OR_UPDATE, ON_UPDATE)
                  .forEach(trigger -> internalFields.putAll(trigger, emptySet()));
        }

        public Builder<E> withName(final String name) {
            this.name = requireNonNull(name, "A name must be provided");
            return this;
        }

        public Builder<E> withUnderlyingExternalFields(final EntityField<?, ?>... externalFields) {
            if (externalFields == null) {
                this.externalFields = emptySet();
                return this;
            } else {
                return withUnderlyingExternalFields(Stream.of(externalFields));
            }
        }

        public Builder<E> withUnderlyingExternalFields(final Stream<? extends EntityField<?, ?>> externalFields) {
            if (externalFields == null) {
                this.externalFields = emptySet();
                return this;
            } else {
                return withExternalFields(externalFields.map(AuditedField::new));
            }
        }

        public Builder<E> withExternalFields(final Stream<? extends AuditedField<?, ?>> externalFields) {
            this.externalFields = externalFields == null ? emptySet() : externalFields.collect(toUnmodifiableSet());
            return this;
        }

        @SafeVarargs
        public final Builder<E> withUnderlyingInternalFields(final AuditTrigger trigger,
                                                             final EntityField<E, ?>... internalFields) {
            final var auditedFields = Arrays.stream(internalFields)
                                            .map(AuditedField::new)
                                            .collect(toUnmodifiableSet());
            this.internalFields.putAll(trigger, auditedFields);
            return this;
        }

        public final Builder<E> withInternalFields(final Map<AuditTrigger, ? extends Collection<AuditedField<E, ?>>> internalFields) {
            if (internalFields != null) {
                internalFields.forEach(this.internalFields::putAll);
            } else {
                this.internalFields.clear();
            }
            return this;
        }

        public AuditedEntityType<E> build() {
            return new AuditedEntityType<>(idField,
                                           name,
                                           externalFields,
                                           internalFields);
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

        final AuditedEntityType<?> that = (AuditedEntityType<?>) o;

        return new EqualsBuilder()
            .append(idField, that.idField)
            .append(name, that.name)
            .append(externalFields, that.externalFields)
            .append(internalFields, that.internalFields)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(idField)
            .append(name)
            .append(externalFields)
            .append(internalFields)
            .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
            .append("idField", idField)
            .append("name", name)
            .append("externalFields", externalFields)
            .append("internalFields", internalFields)
            .toString();
    }

}
