package com.kenshoo.pl.entity.internal.audit;

import com.google.common.annotations.VisibleForTesting;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.spi.audit.AuditExtensions;
import com.kenshoo.pl.entity.spi.audit.AuditFieldValueFormatter;
import com.kenshoo.pl.entity.spi.audit.DefaultAuditFieldValueFormatter;

import static java.util.Objects.requireNonNull;

public class AuditFieldValueResolverFactory {

    public static final AuditFieldValueResolverFactory INSTANCE = new AuditFieldValueResolverFactory();

    private final AuditExtensionsExtractor auditExtensionsExtractor;
    private final AuditFieldValueFormatter defaultFormatter;

    private AuditFieldValueResolverFactory() {
        this(AuditExtensionsExtractor.INSTANCE, DefaultAuditFieldValueFormatter.INSTANCE);
    }

    @VisibleForTesting
    AuditFieldValueResolverFactory(final AuditExtensionsExtractor auditExtensionsExtractor,
                                   final AuditFieldValueFormatter defaultFormatter) {
        this.auditExtensionsExtractor = auditExtensionsExtractor;
        this.defaultFormatter = defaultFormatter;
    }

    public AuditFieldValueResolver create(final EntityType<?> entityType) {
        requireNonNull(entityType, "An EntityType is required");

        final var fieldValueFormatter = auditExtensionsExtractor.extract(entityType)
            .map(AuditExtensions::fieldValueFormatter)
            .orElse(defaultFormatter);
        return new AuditFieldValueResolver(fieldValueFormatter);
    }
}
