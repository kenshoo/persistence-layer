package com.kenshoo.pl.entity.internal.audit;

import com.google.common.annotations.VisibleForTesting;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.PostTransactionEntityIdExtractor;
import com.kenshoo.pl.entity.spi.CurrentStateConsumer;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

public class AuditRecordGenerator<E extends EntityType<E>> implements CurrentStateConsumer<E> {

    private final AuditedFieldsFilter<E> auditedFieldsFilter;
    private final PostTransactionEntityIdExtractor entityIdExtractor;

    public AuditRecordGenerator(final AuditedFieldSet<E> auditedFieldSet) {
        this(new AuditedFieldsFilter<>(requireNonNull(auditedFieldSet, "An auditedFieldSet is required")),
             PostTransactionEntityIdExtractor.INSTANCE);
    }

    @VisibleForTesting
    AuditRecordGenerator(final AuditedFieldsFilter<E> auditedFieldsFilter,
                         final PostTransactionEntityIdExtractor entityIdExtractor) {
        this.auditedFieldsFilter = auditedFieldsFilter;
        this.entityIdExtractor = entityIdExtractor;
    }

    @Override
    public Stream<? extends EntityField<?, ?>> requiredFields(final Collection<? extends EntityField<E, ?>> fieldsToUpdate,
                                                              final ChangeOperation changeOperation) {
        return auditedFieldsFilter.filter(fieldsToUpdate)
                                  .getAllFields()
                                  .stream();
    }

    public AuditRecord<E> generate(final EntityChange<E> entityChange,
                                   final Entity entity,
                                   final Collection<? extends AuditRecord<?>> childRecords) {
        requireNonNull(entityChange, "entityChange is required");
        requireNonNull(entity, "entity is required");

        final AuditRecord.Builder<E> entityChangeRecordBuilder = new AuditRecord.Builder<E>()
            .withEntityType(entityChange.getEntityType())
            .withEntityId(extractEntityId(entityChange, entity))
            .withOperation(entityChange.getChangeOperation());

        final Set<? extends EntityField<E, ?>> auditedFields =
            auditedFieldsFilter.filter(entityChange.getChangedFields().collect(toSet()))
                               .getAuditedFields();

        final Set<? extends FieldAuditRecord<E>> fieldRecords = generateFieldRecords(entityChange,
                                                                                     entity,
                                                                                     auditedFields);
        return entityChangeRecordBuilder.withFieldChanges(fieldRecords)
                                        .withChildChanges(childRecords)
                                        .build();
    }

    @VisibleForTesting
    public AuditedFieldSet<E> getCompleteFieldSet() {
        return auditedFieldsFilter.getCompleteFieldSet();
    }

    private Set<FieldAuditRecord<E>> generateFieldRecords(final EntityChange<E> entityChange,
                                                          final Entity entity,
                                                          final Collection<? extends EntityField<E, ?>> fieldsToLog) {
        return fieldsToLog.stream()
                          .filter(fieldWasChanged(entityChange, entity))
                          .map(field -> buildFieldChangeRecord(entityChange, entity, field))
                          .collect(toSet());
    }

    private FieldAuditRecord<E> buildFieldChangeRecord(EntityChange<E> entityChange, Entity entity, EntityField<E, ?> field) {
        return new FieldAuditRecord<>(field,
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