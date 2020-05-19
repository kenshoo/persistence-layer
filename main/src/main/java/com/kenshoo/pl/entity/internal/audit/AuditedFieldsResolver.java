package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.annotation.Audited;
import com.kenshoo.pl.entity.annotation.NotAudited;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Optional;

import static com.kenshoo.pl.entity.internal.EntityTypeReflectionUtil.isAnnotatedWith;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public class AuditedFieldsResolver {

    private static final Logger logger = LoggerFactory.getLogger(AuditedFieldsResolver.class);

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
        return noFieldsToAudit(entityType);
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
            return noFieldsToAudit(entityType);
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

    private <E extends EntityType<E>> Optional<AuditedFieldSet<E>> noFieldsToAudit(final EntityType<E> entityType) {
        logger.warn("Cannot audit entity type {} because no fields are marked for auditing and/or @Id annotation is missing",
                    entityType);
        return Optional.empty();
    }

    private AuditedFieldsResolver() {
        // Singleton
    }
}
