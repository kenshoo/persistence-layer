package com.kenshoo.pl.entity.spi.audit;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.internal.audit.AuditedField;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * This is the default formatter for field values to be included in an AuditRecord.<br>
 * It formats values using the {@code String} {@link com.kenshoo.pl.entity.ValueConverter} defined for a given field.<br>
 * If no such converter is defined, the default string representation of the value is returned.<br>
 *
 * @see EntityField#getStringValueConverter()
 */
public class DefaultAuditFieldValueFormatter implements AuditFieldValueFormatter {

    public static final AuditFieldValueFormatter INSTANCE = new DefaultAuditFieldValueFormatter();

    @Override
    public <T> String format(AuditedField<?, T> auditedField, T value) {
        requireNonNull(auditedField, "An AuditedField is required");
        requireNonNull(value, "A value is required");
        return Optional.ofNullable(auditedField.getStringValueConverter())
                       .map(converter -> converter.convertTo(value))
                       .orElse(String.valueOf(value));
    }
}
