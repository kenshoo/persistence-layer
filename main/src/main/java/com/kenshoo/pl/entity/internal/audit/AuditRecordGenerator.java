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
import java.util.stream.Stream;

import static com.kenshoo.pl.entity.ChangeOperation.UPDATE;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public class AuditRecordGenerator<E extends EntityType<E>> implements CurrentStateConsumer<E> {

    private final AuditedFieldSet<E> auditedFieldSet;
    private final EntityIdExtractor entityIdExtractor;
    private final AuditedFieldsToFetchResolver auditedFieldsToFetchResolver;

    public AuditRecordGenerator(final AuditedFieldSet<E> auditedFieldSet) {
        this(auditedFieldSet,
             EntityIdExtractor.INSTANCE,
             AuditedFieldsToFetchResolver.INSTANCE);
    }

    @VisibleForTesting
    AuditRecordGenerator(final AuditedFieldSet<E> auditedFieldSet,
                         final EntityIdExtractor entityIdExtractor,
                         final AuditedFieldsToFetchResolver auditedFieldsToFetchResolver) {
        this.auditedFieldSet = requireNonNull(auditedFieldSet, "An audited field set is required");
        this.entityIdExtractor = entityIdExtractor;
        this.auditedFieldsToFetchResolver = auditedFieldsToFetchResolver;
    }

    @Override
    public Stream<? extends EntityField<?, ?>> requiredFields(final Collection<? extends EntityField<E, ?>> fieldsToUpdate,
                                                              final ChangeOperation operator) {
        return auditedFieldsToFetchResolver.resolve(auditedFieldSet, fieldsToUpdate);
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
        requireNonNull(currentState, "currentState is required");

        final String entityId = extractEntityId(entityChange, currentState);

        final FinalEntityState finalState = new FinalEntityState(currentState, entityChange);

        final Collection<? extends EntityFieldValue> mandatoryFieldValues = generateMandatoryFieldValues(finalState);

        final Collection<? extends FieldAuditRecord<E>> fieldRecords = generateChangedFieldRecords(currentState, finalState);

        return new AuditRecord.Builder<E>()
            .withEntityType(entityChange.getEntityType())
            .withEntityId(entityId)
            .withMandatoryFieldValues(mandatoryFieldValues)
            .withOperator(entityChange.getChangeOperation())
            .withFieldRecords(fieldRecords)
            .withChildRecords(childRecords)
            .build();
    }

    private Collection<? extends FieldAuditRecord<E>> generateChangedFieldRecords(final CurrentEntityState currentState, final FinalEntityState finalState) {
        return auditedFieldSet.getInternalFields()
                              .filter(field -> fieldWasChanged(currentState, finalState, field))
                              .map(field -> buildFieldRecord(currentState, finalState, field))
                              .collect(toList());
    }

    private FieldAuditRecord<E> buildFieldRecord(final CurrentEntityState currentState,
                                                 final FinalEntityState finalState,
                                                 final EntityField<E, ?> field) {
        final FieldAuditRecord.Builder<E> fieldRecordBuilder = FieldAuditRecord.builder(field);
        currentState.safeGet(field).ifNotNull(fieldRecordBuilder::oldValue);
        finalState.safeGet(field).ifNotNull(fieldRecordBuilder::newValue);
        return fieldRecordBuilder.build();
    }

    private String extractEntityId(final EntityChange<E> entityChange,
                                   final CurrentEntityState currentState) {
        return entityIdExtractor.extract(entityChange, currentState)
                                .orElseThrow(() -> new IllegalStateException("Could not extract the entity id for entity type '" + entityChange.getEntityType() + "' " +
                                                                                 "from either the EntityChange or the CurrentEntityState, so the audit record cannot be generated."));
    }

    private Collection<? extends EntityFieldValue> generateMandatoryFieldValues(final FinalEntityState finalState) {
        return auditedFieldSet.getMandatoryFields()
                              .map(field -> ImmutablePair.of(field, finalState.safeGet(field)))
                              .filter(pair -> pair.getValue().isNotNull())
                              .map(pair -> new EntityFieldValue(pair.getKey(), pair.getValue().get()))
                              .collect(toList());
    }

    private <T> boolean fieldWasChanged(final CurrentEntityState currentState,
                                        final FinalEntityState finalState,
                                        final EntityField<E, T> field) {
        return !fieldStayedTheSame(currentState, finalState, field);
    }

    private <T> boolean fieldStayedTheSame(final CurrentEntityState currentState,
                                           final FinalEntityState finalState,
                                           final EntityField<E, T> field) {
        return currentState.safeGet(field).equals(finalState.safeGet(field), field::valuesEqual);
    }

    @VisibleForTesting
    public AuditedFieldSet<E> getAuditedFieldSet() {
        return auditedFieldSet;
    }
}