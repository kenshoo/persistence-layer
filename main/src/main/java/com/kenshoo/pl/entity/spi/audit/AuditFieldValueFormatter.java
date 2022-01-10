package com.kenshoo.pl.entity.spi.audit;

import com.kenshoo.pl.entity.internal.audit.AuditedField;

/**
 * A formatter for field values to be included in an audit record.
 */
public interface AuditFieldValueFormatter {

    /**
     * Format the given value of the given audited field
     *
     * @param auditedField the field whose value is to be formatted; required
     * @param value the value to be formatted; must not be {@code null}
     * @param <T> the type of value to be formatted
     * @return the formatted value
     * @throws NullPointerException if one of the inputs is {@code null}
     */
    <T> String format(final AuditedField<?, T> auditedField, final T value);
}
