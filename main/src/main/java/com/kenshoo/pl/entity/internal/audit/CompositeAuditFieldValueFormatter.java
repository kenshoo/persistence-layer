package com.kenshoo.pl.entity.internal.audit;

import com.google.common.annotations.VisibleForTesting;
import com.kenshoo.pl.entity.spi.audit.AuditExtensions;
import com.kenshoo.pl.entity.spi.audit.AuditFieldValueFormatter;
import com.kenshoo.pl.entity.spi.audit.DefaultAuditFieldValueFormatter;

import static java.util.Objects.requireNonNull;

public class CompositeAuditFieldValueFormatter implements AuditFieldValueFormatter {

    public static final AuditFieldValueFormatter INSTANCE = new CompositeAuditFieldValueFormatter();

    private final AuditExtensionsExtractor auditExtensionsExtractor;
    private final AuditFieldValueFormatter defaultFormatter;

    private CompositeAuditFieldValueFormatter() {
        this(AuditExtensionsExtractor.INSTANCE, DefaultAuditFieldValueFormatter.INSTANCE);
    }

    @VisibleForTesting
    CompositeAuditFieldValueFormatter(final AuditExtensionsExtractor auditExtensionsExtractor,
                                      final AuditFieldValueFormatter defaultFormatter) {
        this.auditExtensionsExtractor = auditExtensionsExtractor;
        this.defaultFormatter = defaultFormatter;
    }

    @Override
    public <T> String format(AuditedField<?, T> auditedField, T value) {
        requireNonNull(auditedField, "An AuditedField is required");

        final var formatter = auditExtensionsExtractor.extract(auditedField.getEntityType())
            .map(AuditExtensions::fieldValueFormatter)
            .orElse(defaultFormatter);

        return formatter.format(auditedField, value);
    }
}
