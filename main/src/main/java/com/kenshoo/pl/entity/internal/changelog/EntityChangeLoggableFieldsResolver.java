package com.kenshoo.pl.entity.internal.changelog;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.annotation.ChangesLogged;
import com.kenshoo.pl.entity.annotation.ChangesNotLogged;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.kenshoo.pl.entity.internal.EntityTypeReflectionUtil.isAnnotatedWith;
import static java.util.Objects.requireNonNull;

public class EntityChangeLoggableFieldsResolver {

    public static final EntityChangeLoggableFieldsResolver INSTANCE = new EntityChangeLoggableFieldsResolver();

    public <E extends EntityType<E>> Optional<? extends EntityChangeLoggableFieldSet<E>> resolve(final EntityType<E> entityType) {
        requireNonNull(entityType, "entityType is required");

        final Optional<EntityField<E, ? extends Number>> optionalIdField = entityType.getIdField();
        return optionalIdField.flatMap(idField -> {
            final boolean entityTypeChangeLogEnabled = entityType.getClass().isAnnotationPresent(ChangesLogged.class);
            final Collection<? extends EntityField<E, ?>> additionalFields =
                entityType.getFields()
                          .filter(field -> !field.equals(idField))
                          .filter(field -> shouldLogFieldChanges(entityType,
                                                                 entityTypeChangeLogEnabled,
                                                                 field))
                          .collect(Collectors.toSet());
            return Optional.of(new EntityChangeLoggableFieldSet<>(idField, additionalFields));
        });
    }

    private <E extends EntityType<E>> boolean shouldLogFieldChanges(final EntityType<E> entityType,
                                                                    final boolean entityTypeChangeLogEnabled,
                                                                    final EntityField<E, ?> field) {
        if (entityTypeChangeLogEnabled) {
            return !isAnnotatedWith(entityType, ChangesNotLogged.class, field);
        }
        return isAnnotatedWith(entityType, ChangesLogged.class, field);
    }

    private EntityChangeLoggableFieldsResolver() {
        // Singleton
    }
}
