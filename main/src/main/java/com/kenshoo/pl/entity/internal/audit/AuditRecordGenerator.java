package com.kenshoo.pl.entity.internal.audit;

import com.google.common.annotations.VisibleForTesting;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.audit.AuditRecord;
import com.kenshoo.pl.entity.audit.FieldAuditRecord;
import com.kenshoo.pl.entity.internal.EntityIdExtractor;
import com.kenshoo.pl.entity.spi.CurrentStateConsumer;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static com.kenshoo.pl.entity.ChangeOperation.UPDATE;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public class AuditRecordGenerator<E extends EntityType<E>> implements CurrentStateConsumer<E> {

    private final AuditedFieldSet<E> auditedFieldSet;
    private final EntityIdExtractor entityIdExtractor;

    public AuditRecordGenerator(final AuditedFieldSet<E> auditedFieldSet) {
        this(auditedFieldSet,
             EntityIdExtractor.INSTANCE);
    }

    @VisibleForTesting
    AuditRecordGenerator(final AuditedFieldSet<E> auditedFieldSet,
                         final EntityIdExtractor entityIdExtractor) {
        this.auditedFieldSet = requireNonNull(auditedFieldSet, "An audited field set is required");
        this.entityIdExtractor = entityIdExtractor;
    }

    @Override
    public Stream<? extends EntityField<?, ?>> requiredFields(final Collection<? extends EntityField<E, ?>> fieldsToUpdate,
                                                              final ChangeOperation operator) {
        return auditedFieldSet.intersectWith(fieldsToUpdate.stream())
                              .getAllFields();
    }

    public Optional<? extends AuditRecord<E>> generate(final EntityChange<E> entityChange,
                                                       final CurrentEntityState currentState,
                                                       final Collection<? extends AuditRecord<?>> childRecords) {
        final AuditRecord<E> auditRecord = generateInner(entityChange,
                                                         currentState,
                                                         childRecords);

        if (entityChange.getChangeOperation() == UPDATE && auditRecord.hasNoChanges()) {
            return Optional.empty();
        }
        return Optional.of(auditRecord);
    }

    private AuditRecord<E> generateInner(final EntityChange<E> entityChange,
                                         final CurrentEntityState currentState,
                                         final Collection<? extends AuditRecord<?>> childRecords) {
        requireNonNull(entityChange, "entityChange is required");
        requireNonNull(currentState, "entity is required");

        final String entityId = extractEntityId(entityChange, currentState);

        final Collection<? extends EntityFieldValue> mandatoryFieldValues = generateMandatoryFieldValues(currentState);

        final Set<? extends EntityField<E, ?>> candidateOnChangeFields = auditedFieldSet.intersectWith(entityChange.getChangedFields())
                                                                                        .getOnChangeFields();

        final Collection<? extends FieldAuditRecord<E>> fieldRecords = generateFieldRecords(entityChange,
                                                                                            currentState,
                                                                                            candidateOnChangeFields);

        return new AuditRecord.Builder<E>()
            .withEntityType(entityChange.getEntityType())
            .withEntityId(entityId)
            .withMandatoryFieldValues(mandatoryFieldValues)
            .withOperator(entityChange.getChangeOperation())
            .withFieldRecords(fieldRecords)
            .withChildRecords(childRecords)
            .build();
    }

    private Collection<? extends FieldAuditRecord<E>> generateFieldRecords(final EntityChange<E> entityChange,
                                                                           final CurrentEntityState currentState,
                                                                           final Collection<? extends EntityField<E, ?>> candidateOnChangeFields) {
        return candidateOnChangeFields.stream()
                                      .filter(field -> fieldWasChanged(entityChange, currentState, field))
                                      .map(field -> buildFieldRecord(entityChange, currentState, field))
                                      .collect(toList());
    }

    private FieldAuditRecord<E> buildFieldRecord(final EntityChange<E> entityChange,
                                                 final CurrentEntityState currentState,
                                                 final EntityField<E, ?> field) {
        final FieldAuditRecord.Builder<E> fieldRecordBuilder = FieldAuditRecord.builder(field);
         currentState.safeGet(field).ifFilled(fieldRecordBuilder::oldValue);
        entityChange.safeGet(field).ifFilled(fieldRecordBuilder::newValue);
        return fieldRecordBuilder.build();
    }

    private String extractEntityId(final EntityChange<E> entityChange,
                                   final CurrentEntityState currentState) {
        return entityIdExtractor.extract(entityChange, currentState)
                                .orElseThrow(() -> new IllegalStateException("Could not extract the entity id for entity type '" + entityChange.getEntityType() + "' " +
                                                                                 "from either the EntityChange or the Entity, so the audit record cannot be generated."));
    }

    private Collection<? extends EntityFieldValue> generateMandatoryFieldValues(final CurrentEntityState currentState) {
        return auditedFieldSet.getExternalMandatoryFields().stream()
                              .map(field -> ImmutablePair.of(field,  currentState.safeGet(field)))
                              .filter(pair -> pair.getValue().isFilled())
                              .map(pair -> new EntityFieldValue(pair.getKey(), pair.getValue().get()))
                              .collect(toList());
    }

    private boolean fieldWasChanged(final EntityChange<E> entityChange,
                                    final CurrentEntityState currentState,
                                    final EntityField<E, ?> field) {
        return !fieldStayedTheSame(entityChange, currentState, field);
    }

    private boolean fieldStayedTheSame(final EntityChange<E> entityChange,
                                       final CurrentEntityState currentState,
                                       final EntityField<E, ?> field) {
        return  currentState.containsField(field) && fieldValuesEqual(entityChange, currentState, field);
    }

    private <T> boolean fieldValuesEqual(final EntityChange<E> entityChange,
                                         final CurrentEntityState currentState,
                                         final EntityField<E, T> field) {
        return field.valuesEqual(entityChange.get(field),  currentState.get(field));
    }

    @VisibleForTesting
    public AuditedFieldSet<E> getAuditedFieldSet() {
        return auditedFieldSet;
    }
}