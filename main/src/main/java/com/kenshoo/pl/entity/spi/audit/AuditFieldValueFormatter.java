package com.kenshoo.pl.entity.spi.audit;

import com.kenshoo.pl.entity.EntityField;

/**
 * A formatter for field values to be included in an audit record.
 */
public interface AuditFieldValueFormatter {

    /**
     * Format the given value of the given field for an audit record
     *
     * @param field the field whose value is to be formatted; required
     * @param value the value to be formatted; must not be {@code null}
     * @param <T>   the type of value to be formatted
     * @return the formatted value
     * @throws NullPointerException if any of the inputs is {@code null}
     */
    <T> String format(final EntityField<?, T> field, final T value);

    /**
     * An invalid implementation that should not be used,
     * only exists because we can't provide a default of {@code null} for an annotation parameter
     */
    final class MissingAuditFieldValueFormatter implements AuditFieldValueFormatter {

        @Override
        public <T> String format(EntityField<?, T> field, T value) {
            throw new IllegalStateException("This formatter is only for indicating emptiness of the annotation param and should not be used!!");
        }
    }
}