package com.kenshoo.pl.entity.internal.audit;

import com.google.common.annotations.VisibleForTesting;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.annotation.audit.NotAudited;
import com.kenshoo.pl.entity.audit.AuditTrigger;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.kenshoo.pl.entity.audit.AuditTrigger.ON_CREATE_OR_UPDATE;
import static com.kenshoo.pl.entity.internal.EntityTypeReflectionUtil.getFieldAnnotation;
import static com.kenshoo.pl.entity.internal.EntityTypeReflectionUtil.isAnnotatedWith;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static org.jooq.lambda.Seq.seq;

public class AuditedFieldsResolver {

    public static final AuditedFieldsResolver INSTANCE = new AuditedFieldsResolver(ExternalMandatoryFieldsExtractor.INSTANCE);

    private final ExternalMandatoryFieldsExtractor externalMandatoryFieldsExtractor;

    @VisibleForTesting
    AuditedFieldsResolver(ExternalMandatoryFieldsExtractor externalMandatoryFieldsExtractor) {
        this.externalMandatoryFieldsExtractor = externalMandatoryFieldsExtractor;
    }

    public <E extends EntityType<E>> Optional<AuditedFieldSet<E>> resolve(final E entityType) {
        requireNonNull(entityType, "entityType is required");
        return entityType.getIdField()
                         .flatMap(idField -> resolve(entityType, idField));
    }

    private <E extends EntityType<E>> Optional<AuditedFieldSet<E>> resolve(final E entityType, final EntityField<E, ? extends Number> idField) {
        final boolean entityLevelAudited = entityType.getClass().isAnnotationPresent(Audited.class);

        final Stream<? extends EntityField<?, ?>> externalFields = externalMandatoryFieldsExtractor.extract(entityType);

        final Map<AuditTrigger, List<EntityField<E, ?>>> internalFields = resolveInternalFieldsByTriggers(entityType, idField, entityLevelAudited);

        final AuditedFieldSet<E> fieldSet = AuditedFieldSet.builder(idField)
                                                           .withExternalFields(seq(externalFields))
                                                           .withInternalFields(internalFields)
                                                           .build();

        if (entityLevelAudited || fieldSet.hasInternalFields()) {
            return Optional.of(fieldSet);
        }
        return empty();
    }

    private <E extends EntityType<E>> Map<AuditTrigger, List<EntityField<E, ?>>> resolveInternalFieldsByTriggers(final E entityType,
                                                                                                                 final EntityField<E, ? extends Number> idField,
                                                                                                                 final boolean entityLevelAudited) {
        return entityType.getFields()
                         .filter(field -> !idField.equals(field))
                         .filter(field -> !isAnnotatedWith(field.getEntityType(), NotAudited.class, field))
                         .map(field -> resolveFieldTrigger(field, entityLevelAudited))
                         .filter(Optional::isPresent)
                         .map(Optional::get)
                         .collect(Collectors.groupingBy(AuditedFieldTrigger::getTrigger,
                                                        mapping(AuditedFieldTrigger::getField, toList())));
    }

    private <E extends EntityType<E>> Optional<AuditedFieldTrigger<E>> resolveFieldTrigger(final EntityField<E, ?> field,
                                                                                           final boolean entityLevelAudited) {
        final Optional<AuditTrigger> optionalEntityTrigger = entityLevelAudited ? Optional.of(ON_CREATE_OR_UPDATE) : Optional.empty();
        return Stream.of(extractFieldTrigger(field),
                         optionalEntityTrigger)
                     .filter(Optional::isPresent)
                     .map(Optional::get)
                     .map(trigger -> new AuditedFieldTrigger<>(field, trigger))
                     .findFirst();
    }

    private <E extends EntityType<E>> Optional<AuditTrigger> extractFieldTrigger(final EntityField<E, ?> field) {
        return Optional.ofNullable(getFieldAnnotation(field.getEntityType(), field, Audited.class))
                       .map(Audited::trigger);
    }
}
