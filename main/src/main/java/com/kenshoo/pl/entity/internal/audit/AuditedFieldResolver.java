package com.kenshoo.pl.entity.internal.audit;

import com.google.common.annotations.VisibleForTesting;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.annotation.audit.NotAudited;
import com.kenshoo.pl.entity.audit.AuditTrigger;

import java.util.Optional;

import static com.kenshoo.pl.entity.internal.EntityTypeReflectionUtil.getFieldAnnotation;
import static com.kenshoo.pl.entity.internal.EntityTypeReflectionUtil.isAnnotatedWith;
import static com.kenshoo.pl.entity.internal.audit.AuditIndicator.AUDITED;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

public class AuditedFieldResolver {

    public static final AuditedFieldResolver INSTANCE = new AuditedFieldResolver(AuditFieldNameResolver.INSTANCE,
                                                                                 AuditFieldValueFormatterResolver.INSTANCE);

    private final AuditFieldNameResolver fieldNameResolver;
    private final AuditFieldValueFormatterResolver fieldValueFormatterResolver;

    @VisibleForTesting
    AuditedFieldResolver(final AuditFieldNameResolver auditFieldNameResolver,
                         final AuditFieldValueFormatterResolver fieldValueFormatterResolver) {
        this.fieldNameResolver = auditFieldNameResolver;
        this.fieldValueFormatterResolver = fieldValueFormatterResolver;
    }

    public <E extends EntityType<E>> Optional<AuditedField<E, ?>> resolve(final EntityField<E, ?> field,
                                                                          final AuditIndicator entityAuditIndicator) {
        requireNonNull(field, "field is required");
        requireNonNull(entityAuditIndicator, "entityAuditIndicator is required");

        return Optional.of(field)
                       .filter(not(EntityField::isVirtual))
                       .filter(f -> !isAnnotatedWith(f.getEntityType(), NotAudited.class, f))
                       .filter(f -> isAnnotatedWith(f.getEntityType(), Audited.class, f) || entityAuditIndicator == AUDITED)
                       .map(this::toAuditedField);
    }

    private <E extends EntityType<E>> AuditedField<E, ?> toAuditedField(EntityField<E, ?> field) {
        final var auditedFieldBuilder = AuditedField.builder(field)
                                                    .withName(fieldNameResolver.resolve(field))
                                                    .withValueFormatter(fieldValueFormatterResolver.resolve(field));
        resolveFieldTrigger(field).ifPresent(auditedFieldBuilder::withTrigger);
        return auditedFieldBuilder.build();
    }

    private <E extends EntityType<E>> Optional<AuditTrigger> resolveFieldTrigger(final EntityField<E, ?> field) {
        return Optional.ofNullable(getFieldAnnotation(field.getEntityType(), field, Audited.class))
                       .map(Audited::trigger);
    }
}
