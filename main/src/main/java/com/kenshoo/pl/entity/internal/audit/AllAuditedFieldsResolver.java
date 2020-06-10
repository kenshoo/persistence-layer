package com.kenshoo.pl.entity.internal.audit;

import com.google.common.annotations.VisibleForTesting;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.spi.audit.AuditExtensions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.stream.Stream;

import static org.jooq.lambda.Seq.seq;

public class AllAuditedFieldsResolver implements AuditedFieldsResolver {

    private static final Logger logger = LoggerFactory.getLogger(AllAuditedFieldsResolver.class);

    public static final AuditedFieldsResolver INSTANCE = new AllAuditedFieldsResolver(SelfAuditedFieldsResolver.INSTANCE);

    private final AuditedFieldsResolver selfAuditedFieldsResolver;

    @VisibleForTesting
    AllAuditedFieldsResolver(final AuditedFieldsResolver selfAuditedFieldsResolver) {
        this.selfAuditedFieldsResolver = selfAuditedFieldsResolver;
    }

    @Override
    public <E extends EntityType<E>> Optional<? extends AuditedFieldSet<E>> resolve(final E entityType) {
        return selfAuditedFieldsResolver.resolve(entityType)
                                        .map(internalFieldSet -> resolveAndSetExternalMandatoryFields(entityType, internalFieldSet));
    }

    private <E extends EntityType<E>> AuditedFieldSet<E> resolveAndSetExternalMandatoryFields(final E entityType,
                                                                                              final AuditedFieldSet<E> internalFieldSet) {
        return internalFieldSet.setExternalMandatoryFields(seq(resolveExternalMandatoryFields(entityType)));
    }

    private Stream<? extends EntityField<?, ?>> resolveExternalMandatoryFields(final EntityType<?> entityType) {
        return Optional.ofNullable(entityType.getClass().getAnnotation(Audited.class))
                       .map(Audited::extensions)
                       .flatMap(this::createExtensions)
                       .map(AuditExtensions::externalMandatoryFields)
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
}
