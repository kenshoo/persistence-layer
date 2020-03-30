package com.kenshoo.pl.entity.internal;

import com.google.common.annotations.VisibleForTesting;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.spi.CurrentStateConsumer;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

public class EntityChangeRecordGenerator<E extends EntityType<E>> implements CurrentStateConsumer<E> {

    private final EntityChangeLoggableFieldsFilter<E> loggableFieldsFilter;
    private final PostTransactionEntityIdExtractor entityIdExtractor;

    public EntityChangeRecordGenerator(final EntityChangeLoggableFieldSet<E> loggableFieldSet) {
        this(new EntityChangeLoggableFieldsFilter<>(requireNonNull(loggableFieldSet, "A loggableFieldSet is required")),
             PostTransactionEntityIdExtractor.INSTANCE);
    }

    @VisibleForTesting
    EntityChangeRecordGenerator(final EntityChangeLoggableFieldsFilter<E> loggableFieldsFilter,
                                final PostTransactionEntityIdExtractor entityIdExtractor) {
        this.loggableFieldsFilter = loggableFieldsFilter;
        this.entityIdExtractor = entityIdExtractor;
    }

    @Override
    public Stream<? extends EntityField<?, ?>> requiredFields(final Collection<? extends EntityField<E, ?>> fieldsToUpdate,
                                                              final ChangeOperation changeOperation) {
        return loggableFieldsFilter.filter(fieldsToUpdate)
                                   .getAllFields()
                                   .stream();
    }

    public EntityChangeRecord<E> generate(final EntityChange<E> entityChange, final Entity entity) {
        requireNonNull(entityChange, "entityChange is required");
        requireNonNull(entity, "entity is required");

        final EntityChangeRecord.Builder<E> entityChangeRecordBuilder = new EntityChangeRecord.Builder<E>()
            .withEntityType(entityChange.getEntityType())
            .withEntityId(extractEntityId(entityChange, entity))
            .withOperation(entityChange.getChangeOperation());

        final Set<? extends EntityField<E, ?>> loggableAdditionalFields =
            loggableFieldsFilter.filter(entityChange.getChangedFields().collect(toSet()))
                                .getAdditionalFields();

        final Set<? extends EntityFieldChangeRecord<E>> fieldChangeRecords = buildFieldChangeRecords(entityChange,
                                                                                                     entity,
                                                                                                     loggableAdditionalFields);
        return entityChangeRecordBuilder.withFieldChanges(fieldChangeRecords)
                                        .build();
    }

    @VisibleForTesting
    public EntityChangeLoggableFieldSet<E> getFullLoggableFieldSet() {
        return loggableFieldsFilter.getCompleteLoggableFieldSet();
    }

    private Set<EntityFieldChangeRecord<E>> buildFieldChangeRecords(final EntityChange<E> entityChange,
                                                                    final Entity entity,
                                                                    final Collection<? extends EntityField<E, ?>> fieldsToLog) {
        return fieldsToLog.stream()
                          .filter(fieldWasChanged(entityChange, entity))
                          .map(field -> buildFieldChangeRecord(entityChange, entity, field))
                          .collect(toSet());
    }

    private EntityFieldChangeRecord<E> buildFieldChangeRecord(EntityChange<E> entityChange, Entity entity, EntityField<E, ?> field) {
        return new EntityFieldChangeRecord<>(field,
                                             entity.containsField(field) ? entity.get(field) : null,
                                             entityChange.get(field));
    }

    private Number extractEntityId(final EntityChange<E> entityChange,
                                   final Entity entity) {
        return entityIdExtractor.extractEntityId(entityChange, entity)
                                .orElseThrow(() -> new IllegalStateException("Could not extract the entity id for entity type '" + entityChange.getEntityType() + "' " +
                                                                                 "from either the EntityChange or the Entity, so the changelog cannot be generated."));
    }

    private Predicate<EntityField<E, ?>> fieldWasChanged(final EntityChange<E> entityChange,
                                                         final Entity entity) {
        return fieldStayedTheSame(entityChange, entity).negate();
    }

    private Predicate<EntityField<E, ?>> fieldStayedTheSame(final EntityChange<E> entityChange,
                                                            final Entity entity) {
        return field -> entity.containsField(field) && Objects.equals(entityChange.get(field), entity.get(field));
    }
}