package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.annotation.audit.NotAudited;
import com.kenshoo.pl.entity.spi.audit.AlwaysAuditedFieldsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.kenshoo.pl.entity.internal.EntityTypeReflectionUtil.isAnnotatedWith;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public class AuditedFieldsResolver {

    private static final Logger logger = LoggerFactory.getLogger(AuditedFieldsResolver.class);

    public static final AuditedFieldsResolver INSTANCE = new AuditedFieldsResolver();

    public <E extends EntityType<E>> Optional<? extends AuditedFieldSet<E>> resolve(final E entityType) {
        requireNonNull(entityType, "entityType is required");

        return entityType.getIdField()
                         .flatMap(idField -> resolve(entityType, idField));
    }

    private <E extends EntityType<E>> Optional<AuditedFieldSet<E>> resolve(final E entityType,
                                                                           final EntityField<E, ? extends Number> idField) {
        final boolean entityTypeAudited = entityType.getClass().isAnnotationPresent(Audited.class);

        final Collection<? extends EntityField<?, ?>> alwaysFields = resolveAlwaysFields(entityType);
        final Collection<? extends EntityField<E, ?>> onChangeFields = resolveOnChangeFields(entityType,
                                                                                             idField,
                                                                                             entityTypeAudited);

        if (hasFieldsToAudit(entityTypeAudited, onChangeFields)) {
            return Optional.of(AuditedFieldSet.builder(idField)
                                              .withAlwaysFields(alwaysFields)
                                              .withOnChangeFields(onChangeFields)
                                              .build());
        }
        return Optional.empty();
    }

    private Collection<? extends EntityField<?, ?>> resolveAlwaysFields(final EntityType<?> entityType) {
        return Optional.ofNullable(entityType.getClass().getAnnotation(Audited.class))
                       .map(Audited::alwaysAuditedFieldsProvider)
                       .flatMap(this::createAlwaysAuditedFieldsProvider)
                       .map(AlwaysAuditedFieldsProvider::getFields)
                       .map(fields -> fields.collect(toList()))
                       .orElse(emptyList());
        }

    private Optional<AlwaysAuditedFieldsProvider> createAlwaysAuditedFieldsProvider(final Class<? extends AlwaysAuditedFieldsProvider> alwaysAuditedFieldsProviderClass) {
        try {
            return Optional.of(alwaysAuditedFieldsProviderClass.getDeclaredConstructor().newInstance());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            logger.error("Failed to create an instance of type {} - either it doesn't have a no-arg constructor, or the constructor is not public. The corresponding fields will not be included in the audit records.",
                         alwaysAuditedFieldsProviderClass,
                         e);
            return Optional.empty();
        }
    }

    private <E extends EntityType<E>> List<EntityField<E, ?>> resolveOnChangeFields(E entityType, EntityField<E, ? extends Number> idField, boolean entityTypeAudited) {
        return entityType.getFields()
                         .filter(field -> !field.equals(idField))
                         .filter(field -> isFieldAudited(entityType,
                                                         entityTypeAudited,
                                                         field))
                         .collect(toList());
    }

    private <E extends EntityType<E>> boolean isFieldAudited(final E entityType,
                                                             final boolean entityTypeAudited,
                                                             final EntityField<E, ?> field) {
        if (entityTypeAudited) {
            return !isAnnotatedWith(entityType, NotAudited.class, field);
        }
        return isAnnotatedWith(entityType, Audited.class, field);
    }

    private <E extends EntityType<E>> boolean hasFieldsToAudit(boolean entityTypeAudited,
                                                               final Collection<? extends EntityField<E, ?>> onChangeFields) {
        return entityTypeAudited || !onChangeFields.isEmpty();
    }

    private AuditedFieldsResolver() {
        // Singleton
    }
}
