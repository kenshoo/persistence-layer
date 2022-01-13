package com.kenshoo.pl.entity.audit;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.spi.audit.AuditFieldValueFormatter;
import com.kenshoo.pl.entity.spi.audit.DefaultAuditFieldValueFormatter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * A class representing a field from one entity which should be included in the audit record of another entity.
 *
 * @param <E> the entity type of the external field
 * @param <T> the type of value in the field
 *
 * @see com.kenshoo.pl.entity.spi.audit.AuditExtensions
 */
public class ExternalAuditedField<E extends EntityType<E>, T> {

    private final EntityField<E, T> field;
    private final String name;
    private final AuditFieldValueFormatter valueFormatter;

    /**
     * @param field the underlying field; required
     * @deprecated use the {@link Builder} instead
     */
    @Deprecated
    public ExternalAuditedField(final EntityField<E, T> field) {
        this(requireNonNull(field, "An underlying field must be provided"),
             field.toString());
    }

    /**
     * @param field the underlying field; required
     * @param name the name to use for this field in the audit record, overriding the default string representation; required
     * @deprecated use the {@link Builder} instead
     */
    @Deprecated
    public ExternalAuditedField(final EntityField<E, T> field,
                                final String name) {
        this(field, name, DefaultAuditFieldValueFormatter.INSTANCE);
    }

    private ExternalAuditedField(final EntityField<E, T> field,
                                 final String name,
                                 final AuditFieldValueFormatter valueFormatter) {
        this.field = requireNonNull(field, "An underlying field must be provided");
        this.name = requireNonNull(name, "A name must be provided");
        this.valueFormatter = valueFormatter;
    }

    /**
     * @return the underlying field of this external audited field
     */
    public EntityField<E, T> getField() {
        return field;
    }

    /**
     * @return the name to use for this field in the audit record
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return the formatter to use for the value of this field in the audit record;
     * if empty, the formatter will be resolved according to the underlying field
     */
    public Optional<AuditFieldValueFormatter> getValueFormatter() {
        return Optional.ofNullable(valueFormatter);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ExternalAuditedField<?, ?> that = (ExternalAuditedField<?, ?>) o;

        return new EqualsBuilder().append(field, that.field).append(name, that.name).append(valueFormatter, that.valueFormatter).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(field).append(name).append(valueFormatter).toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("field", field)
            .append("name", name)
            .append("valueFormatter", valueFormatter)
            .toString();
    }

    public static class Builder<E extends EntityType<E>, T> {
        private final EntityField<E, T> field;
        private String name;
        private AuditFieldValueFormatter valueFormatter;

        /**
         * @param field the underlying field of this external audited field; required
         */
        public Builder(final EntityField<E, T> field) {
            this.field = requireNonNull(field, "An underlying field must be provided");
            this.name = field.toString();
        }

        /**
         * @param name a name to use for this field in the audit record, overriding the default string representation
         */
        public Builder<E, T> withName(final String name) {
            this.name = name;
            return this;
        }

        /**
         * @param valueFormatter a formatter to use for the value of this field in the audit record,
         *                      overriding {@link DefaultAuditFieldValueFormatter}
         */
        public Builder<E, T> withValueFormatter(final AuditFieldValueFormatter valueFormatter) {
            this.valueFormatter = valueFormatter;
            return this;
        }

        public ExternalAuditedField<E, T> build() {
            return new ExternalAuditedField<>(field, name, valueFormatter);
        }
    }
}
