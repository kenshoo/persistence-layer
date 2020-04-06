package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.annotation.Audited;
import com.kenshoo.pl.entity.annotation.NotAudited;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.kenshoo.pl.entity.internal.EntityTypeReflectionUtil.isAnnotatedWith;
import static java.util.Objects.requireNonNull;

public class AuditedFieldsResolver {

    public static final AuditedFieldsResolver INSTANCE = new AuditedFieldsResolver();

    public <E extends EntityType<E>> Optional<? extends AuditedFieldSet<E>> resolve(final EntityType<E> entityType) {
        requireNonNull(entityType, "entityType is required");

        final Optional<EntityField<E, ? extends Number>> optionalIdField = entityType.getIdField();
        return optionalIdField.flatMap(idField -> {
            final boolean entityTypeAudited = entityType.getClass().isAnnotationPresent(Audited.class);
            final Collection<? extends EntityField<E, ?>> additionalFields =
                entityType.getFields()
                          .filter(field -> !field.equals(idField))
                          .filter(field -> isFieldAudited(entityType,
                                                          entityTypeAudited,
                                                          field))
                          .collect(Collectors.toSet());
            return Optional.of(new AuditedFieldSet<>(idField, additionalFields));
        });
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
