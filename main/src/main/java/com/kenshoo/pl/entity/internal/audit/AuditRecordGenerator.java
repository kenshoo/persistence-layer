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

    private final AuditedFieldSet<E> auditedFieldSet;
    private final PostTransactionEntityIdExtractor entityIdExtractor;

    public AuditRecordGenerator(final AuditedFieldSet<E> auditedFieldSet) {
        this(auditedFieldSet,
             PostTransactionEntityIdExtractor.INSTANCE);
    }

    @VisibleForTesting
    AuditRecordGenerator(final AuditedFieldSet<E> auditedFieldSet,
                         final PostTransactionEntityIdExtractor entityIdExtractor) {
        this.auditedFieldSet = requireNonNull(auditedFieldSet, "An audited field set is required");
        this.entityIdExtractor = entityIdExtractor;
    }

    @Override
    public Stream<? extends EntityField<?, ?>> requiredFields(final Collection<? extends EntityField<E, ?>> fieldsToUpdate,
                                                              final ChangeOperation changeOperation) {
        return auditedFieldSet.intersectWith(fieldsToUpdate)
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
            auditedFieldSet.intersectWith(entityChange.getChangedFields().collect(toSet()))
                           .getDataFields();

        final Set<? extends FieldAuditRecord<E>> fieldRecords = generateFieldRecords(entityChange,
                                                                                     entity,
                                                                                     auditedFields);
        return entityChangeRecordBuilder.withFieldChanges(fieldRecords)
                                        .withChildChanges(childRecords)
                                        .build();
    }

    @VisibleForTesting
    public AuditedFieldSet<E> getAuditedFieldSet() {
        return auditedFieldSet;
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

    private String extractEntityId(final EntityChange<E> entityChange,
                                   final Entity entity) {
        return entityIdExtractor.extract(entityChange, entity)
                                .orElseThrow(() -> new IllegalStateException("Could not extract the entity id for entity type '" + entityChange.getEntityType() + "' " +
                                                                                 "from either the EntityChange or the Entity, so the audit record cannot be generated."));
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