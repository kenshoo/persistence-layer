package com.kenshoo.pl.entity.spi.audit;

import com.kenshoo.pl.entity.EntityField;

import java.util.Objects;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

/**
 * A formatter for audit field values which converts the value into its DB representation using the
 * {@link com.kenshoo.pl.entity.EntityFieldDbAdapter} defined for the entity, and finally to a string.<br>
 * The DB adapter uses the {@link com.kenshoo.pl.entity.ValueConverter} defined for the entity to perform the conversion.<br>
 * If no such converter is defined, the default string representation of the value is returned.<br>
 * Note that there could be more than one DB value mapped to a single entity value, and in that case the values will be joined by a semicolon.
 */
public class DBAdapterAuditFieldValueFormatter implements AuditFieldValueFormatter {

    @Override
    public <T> String format(final EntityField<?, T> field, final T value) {
        requireNonNull(field, "A field is required");
        requireNonNull(value, "A value is required");

        return field.getDbAdapter()
                    .getDbValues(value)
                    .map(Objects::toString)
                    .collect(joining(";"));
    }
}
