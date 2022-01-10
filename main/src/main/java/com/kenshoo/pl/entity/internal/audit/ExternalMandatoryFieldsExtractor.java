package com.kenshoo.pl.entity.internal.audit;

import com.google.common.annotations.VisibleForTesting;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.audit.ExternalAuditedField;
import com.kenshoo.pl.entity.spi.audit.AuditExtensions;

import java.util.stream.Stream;

public class ExternalMandatoryFieldsExtractor {

    static final ExternalMandatoryFieldsExtractor INSTANCE = new ExternalMandatoryFieldsExtractor();

    private final AuditExtensionsExtractor auditExtensionsExtractor;

    private ExternalMandatoryFieldsExtractor() {
        this(AuditExtensionsExtractor.INSTANCE);
    }

    @VisibleForTesting
    ExternalMandatoryFieldsExtractor(final AuditExtensionsExtractor auditExtensionsExtractor) {
        this.auditExtensionsExtractor = auditExtensionsExtractor;
    }

    public Stream<? extends AuditedField<?, ?>> extract(final EntityType<?> entityType) {
        return auditExtensionsExtractor.extract(entityType)
                                       .map(AuditExtensions::externalMandatoryFields)
                                       .map(fields -> fields.map(this::toAuditedField))
                                       .orElse(Stream.empty());
    }

    private AuditedField<?, ?> toAuditedField(final ExternalAuditedField<?, ?> externalAuditedField) {
        return AuditedField.builder(externalAuditedField.getField())
                           .withName(externalAuditedField.getName())
                           .build();
    }
}
