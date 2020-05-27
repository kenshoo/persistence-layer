package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.annotation.audit.NotAudited;

import java.util.Collection;
import java.util.Optional;

import static com.kenshoo.pl.entity.internal.EntityTypeReflectionUtil.isAnnotatedWith;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public class AuditedFieldsResolver {

    public static final AuditedFieldsResolver INSTANCE = new AuditedFieldsResolver();

    public <E extends EntityType<E>> Optional<? extends AuditedFieldSet<E>> resolve(final EntityType<E> entityType) {
        requireNonNull(entityType, "entityType is required");

        return entityType.getIdField()
                         .flatMap(idField -> resolve(entityType, idField));
    }

    private <E extends EntityType<E>> Optional<AuditedFieldSet<E>> resolve(final EntityType<E> entityType,
                                                                           final EntityField<E, ? extends Number> idField) {
        final boolean entityTypeAudited = entityType.getClass().isAnnotationPresent(Audited.class);
        final Collection<? extends EntityField<E, ?>> dataFields = entityType.getFields()
                                                                             .filter(field -> !field.equals(idField))
                                                                             .collect(toList());
        if (dataFields.isEmpty()) {
            return resolve(entityType,
                           entityTypeAudited,
                           idField);
        }
        return resolve(entityType,
                       entityTypeAudited,
                       idField,
                       dataFields);
    }

    private <E extends EntityType<E>> Optional<AuditedFieldSet<E>> resolve(final EntityType<E> entityType,
                                                                           final boolean entityTypeAudited,
                                                                           final EntityField<E, ? extends Number> idField) {
        if (entityTypeAudited) {
            return Optional.of(AuditedFieldSet.builder(idField).build());
        }
        return Optional.empty();
    }

    private <E extends EntityType<E>> Optional<AuditedFieldSet<E>> resolve(final EntityType<E> entityType,
                                                                           final boolean entityTypeAudited,
                                                                           final EntityField<E, ? extends Number> idField,
                                                                           final Collection<? extends EntityField<E, ?>> dataFields) {
        final Collection<? extends EntityField<E, ?>> auditedDataFields =
            dataFields.stream()
                      .filter(field -> isFieldAudited(entityType,
                                                      entityTypeAudited,
                                                      field))
                      .collect(toList());
        if (auditedDataFields.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(AuditedFieldSet.builder(idField)
                                          .withDataFields(auditedDataFields)
                                          .build());
    }

    private <E extends EntityType<E>> boolean isFieldAudited(final EntityType<E> entityType,
                                                             final boolean entityTypeAudited,
                                                             final EntityField<E, ?> field) {
        if (entityTypeAudited) {
            return !isAnnotatedWith(entityType, NotAudited.class, field);
        }
        return isAnnotatedWith(entityType, Audited.class, field);
    }

    private AuditedFieldsResolver() {
        // Singleton
    }
}
