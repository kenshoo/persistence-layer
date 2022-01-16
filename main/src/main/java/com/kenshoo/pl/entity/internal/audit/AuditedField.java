package com.kenshoo.pl.entity.internal.audit;

import com.google.common.annotations.VisibleForTesting;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.audit.AuditTrigger;
import com.kenshoo.pl.entity.spi.audit.AuditFieldValueFormatter;
import com.kenshoo.pl.entity.spi.audit.DefaultAuditFieldValueFormatter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import static com.kenshoo.pl.entity.audit.AuditTrigger.ON_CREATE_OR_UPDATE;
import static java.util.Objects.requireNonNull;

public class AuditedField<E extends EntityType<E>, T> {

    private final EntityField<E, T> field;
    private final String name;
    private final AuditFieldValueFormatter valueFormatter;
    private final AuditTrigger trigger;

    private AuditedField(final EntityField<E, T> field,
                         final String name,
                         final AuditFieldValueFormatter valueFormatter,
                         final AuditTrigger trigger) {
        this.field = requireNonNull(field, "An underlying field must be provided");
        this.name = requireNonNull(name, "A name must be provided");
        this.valueFormatter = requireNonNull(valueFormatter, "A value formatter must be provided");
        this.trigger = requireNonNull(trigger, "A trigger must be provided");
    }

    public EntityField<E, T> getField() {
        return field;
    }

    public String getName() {
        return name;
    }

    public AuditTrigger getTrigger() {
        return trigger;
    }

    public String formatValue(final T value) {
        return valueFormatter.format(field, value);
    }

    public boolean valuesEqual(final T v1, final T v2) {
        return field.valuesEqual(v1, v2);
    }

    public static <E extends EntityType<E>, T> Builder<E, T> builder(final EntityField<E, T> field) {
        return new Builder<>(field);
    }

    @VisibleForTesting
    public AuditFieldValueFormatter getValueFormatter() {
        return valueFormatter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final AuditedField<?, ?> that = (AuditedField<?, ?>) o;

        return new EqualsBuilder()
            .append(field, that.field)
            .append(name, that.name)
            .append(valueFormatter, that.valueFormatter)
            .append(trigger, that.trigger)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(field)
            .append(name)
            .append(valueFormatter)
            .append(trigger)
            .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("field", field)
            .append("name", name)
            .append("valueFormatterClass", valueFormatter.getClass().getCanonicalName())
            .append("trigger", trigger)
            .toString();
    }

    public static class Builder<E extends EntityType<E>, T> {
        private final EntityField<E, T> field;
        private String name;
        private AuditFieldValueFormatter valueFormatter;
        private AuditTrigger trigger;

        public Builder(final EntityField<E, T> field) {
            this.field = requireNonNull(field, "An underlying field must be provided");
            this.name = field.toString();
            this.valueFormatter = DefaultAuditFieldValueFormatter.INSTANCE;
            this.trigger = ON_CREATE_OR_UPDATE;
        }

        public Builder<E, T> withName(final String name) {
            this.name = name;
            return this;
        }

        public Builder<E, T> withValueFormatter(final AuditFieldValueFormatter valueFormatter) {
            this.valueFormatter = valueFormatter;
            return this;
        }

        public Builder<E, T> withTrigger(final AuditTrigger trigger) {
            this.trigger = trigger;
            return this;
        }

        public AuditedField<E, T> build() {
            return new AuditedField<>(field, name, valueFormatter, trigger);
        }
    }
}
