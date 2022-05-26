package com.kenshoo.pl.entity.internal.audit;

import com.google.common.annotations.VisibleForTesting;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.audit.AuditTrigger;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.kenshoo.pl.entity.internal.audit.AuditIndicator.AUDITED;
import static com.kenshoo.pl.entity.internal.audit.AuditIndicator.NOT_AUDITED;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toUnmodifiableList;

public class AuditedEntityTypeResolverImpl implements AuditedEntityTypeResolver {

    public static final AuditedEntityTypeResolverImpl INSTANCE = new AuditedEntityTypeResolverImpl(AuditedEntityTypeNameResolver.INSTANCE,
                                                                                                   AuditedFieldResolver.INSTANCE,
                                                                                                   ExternalMandatoryFieldsExtractor.INSTANCE);

    private final AuditedEntityTypeNameResolver auditedEntityTypeNameResolver;
    private final AuditedFieldResolver auditedFieldResolver;
    private final ExternalMandatoryFieldsExtractor externalMandatoryFieldsExtractor;

    @VisibleForTesting
    AuditedEntityTypeResolverImpl(final AuditedEntityTypeNameResolver auditedEntityTypeNameResolver,
                                  final AuditedFieldResolver auditedFieldResolver,
                                  final ExternalMandatoryFieldsExtractor externalMandatoryFieldsExtractor) {
        this.auditedEntityTypeNameResolver = auditedEntityTypeNameResolver;
        this.auditedFieldResolver = auditedFieldResolver;
        this.externalMandatoryFieldsExtractor = externalMandatoryFieldsExtractor;
    }

    @Override
    public <E extends EntityType<E>> Optional<AuditedEntityType<E>> resolve(final E entityType) {
        requireNonNull(entityType, "entityType is required");

        final var entityAuditIndicator = entityType.getClass().isAnnotationPresent(Audited.class) ? AUDITED : NOT_AUDITED;

        final var externalFields = externalMandatoryFieldsExtractor.extract(entityType);

        final var internalFields = resolveInternalFields(entityType, entityAuditIndicator);

        final var auditedEntityType = AuditedEntityType.builder(entityType)
                                                       .withName(auditedEntityTypeNameResolver.resolve(entityType))
                                                       .withExternalFields(externalFields)
                                                       .withInternalFields(internalFields)
                                                       .build();

        if (entityAuditIndicator == AUDITED || auditedEntityType.hasInternalFields()) {
            return Optional.of(auditedEntityType);
        }
        return empty();
    }

    private <E extends EntityType<E>> Map<AuditTrigger, List<AuditedField<E, ?>>> resolveInternalFields(final E entityType,
                                                                                                        final AuditIndicator entityAuditIndicator) {
        return entityType.getFields()
                         .filter(field -> nonIdField(entityType, field))
                         .map(field -> auditedFieldResolver.resolve(field, entityAuditIndicator))
                         .flatMap(Optional::stream)
                         .collect(groupingBy(AuditedField::getTrigger, toUnmodifiableList()));
    }

    private <E extends EntityType<E>> boolean nonIdField(final E entityType, final EntityField<E, ?> field) {
        return entityType.getIdField()
                .filter(idField -> Objects.equals(field, idField))
                .isEmpty();
    }
}
