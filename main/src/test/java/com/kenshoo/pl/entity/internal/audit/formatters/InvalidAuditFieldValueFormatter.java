package com.kenshoo.pl.entity.internal.audit.formatters;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.spi.audit.AuditFieldValueFormatter;

import static java.util.Objects.requireNonNull;

public class InvalidAuditFieldValueFormatter implements AuditFieldValueFormatter {

    public InvalidAuditFieldValueFormatter(final String ignored) {
        // Dummy non-default ctor. to make the reflection fail when trying to instantiate this class
    }

    @Override
    public <T> String format(final EntityField<?, T> field, final T value) {
        requireNonNull(field, "field is required");
        requireNonNull(value, "value is required");

        return value + " blabla";
    }
}
