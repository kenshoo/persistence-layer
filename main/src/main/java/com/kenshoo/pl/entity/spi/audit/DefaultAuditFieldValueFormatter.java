package com.kenshoo.pl.entity.spi.audit;

import com.kenshoo.pl.entity.EntityField;

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

    private DefaultAuditFieldValueFormatter() {
        // singleton
    }

    @Override
    public <T> String format(final EntityField<?, T> field, final T value) {
        requireNonNull(field, "A field is required");
        requireNonNull(value, "A value is required");

        return Optional.ofNullable(field.getStringValueConverter())
                       .map(converter -> converter.convertTo(value))
                       .orElse(String.valueOf(value));
    }
}