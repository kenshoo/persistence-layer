package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.spi.audit.AuditExtensions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.stream.Stream;

import static com.kenshoo.pl.entity.audit.AuditTrigger.ALWAYS;

public class ExternalMandatoryFieldsExtractor {

    private static final Logger logger = LoggerFactory.getLogger(ExternalMandatoryFieldsExtractor.class);

    static final ExternalMandatoryFieldsExtractor INSTANCE = new ExternalMandatoryFieldsExtractor();

    public Stream<? extends AuditedField<?, ?>> extract(final EntityType<?> entityType) {
        return Optional.ofNullable(entityType.getClass().getAnnotation(Audited.class))
                       .map(Audited::extensions)
                       .flatMap(this::createExtensions)
                       .map(AuditExtensions::externalMandatoryFields)
                       .map(fields -> fields.map(this::toAuditedField))
                       .orElse(Stream.empty());
    }

    private Optional<AuditExtensions> createExtensions(final Class<? extends AuditExtensions> extensionsClass) {
        try {
            return Optional.of(extensionsClass.getDeclaredConstructor().newInstance());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            logger.error("Failed to create an instance of type {} - either it doesn't have a no-arg constructor, or the constructor is not public. The corresponding fields will not be included in the audit records.",
                         extensionsClass,
                         e);
            return Optional.empty();
        }
    }

    private AuditedField<?, ?> toAuditedField(final EntityField<?, ?> field) {
        return AuditedField.builder(field)
                           .withTrigger(ALWAYS)
                           .build();
    }
}
