package com.kenshoo.pl.entity.internal.audit;

import com.google.common.annotations.VisibleForTesting;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.audit.AuditRecord;
import com.kenshoo.pl.entity.audit.FieldAuditRecord;
import com.kenshoo.pl.entity.internal.EntityIdExtractor;

import java.util.Collection;
import java.util.Optional;

import static com.kenshoo.pl.entity.ChangeOperation.UPDATE;
import static java.util.Objects.requireNonNull;

public class AuditRecordGeneratorImpl<E extends EntityType<E>> implements AuditRecordGenerator<E> {

    private final AuditMandatoryFieldValuesGenerator mandatoryFieldValuesGenerator;
    private final AuditFieldChangesGenerator<E> fieldChangesGenerator;
    private final EntityIdExtractor entityIdExtractor;
    private final String entityTypeName;

    public AuditRecordGeneratorImpl(final AuditMandatoryFieldValuesGenerator mandatoryFieldValuesGenerator,
                                    final AuditFieldChangesGenerator<E> fieldChangesGenerator,
                                    final String entityTypeName) {
        this(mandatoryFieldValuesGenerator,
             fieldChangesGenerator,
             EntityIdExtractor.INSTANCE,
             entityTypeName);
    }

    @VisibleForTesting
    AuditRecordGeneratorImpl(final AuditMandatoryFieldValuesGenerator mandatoryFieldValuesGenerator,
                             final AuditFieldChangesGenerator<E> fieldChangesGenerator,
                             final EntityIdExtractor entityIdExtractor,
                             final String entityTypeName) {
        this.mandatoryFieldValuesGenerator = mandatoryFieldValuesGenerator;
        this.fieldChangesGenerator = fieldChangesGenerator;
        this.entityIdExtractor = entityIdExtractor;
        this.entityTypeName = entityTypeName;
    }

    @Override
    public Optional<AuditRecord<E>> generate(final EntityChange<E> entityChange,
                                             final ChangeContext context,
                                             final Collection<? extends AuditRecord<?>> childRecords) {
        requireNonNull(entityChange, "entityChange is required");

        final AuditRecord<E> auditRecord = generateInner(entityChange,
                                                         context,
                                                         childRecords);

        if (entityChange.getChangeOperation() == UPDATE && auditRecord.hasNoChanges()) {
            return Optional.empty();
        }
        return Optional.of(auditRecord);
    }

    private AuditRecord<E> generateInner(final EntityChange<E> entityChange,
                                         final ChangeContext context,
                                         final Collection<? extends AuditRecord<?>> childRecords) {
        requireNonNull(context, "context is required");

        final CurrentEntityState currentState = context.getEntity(entityChange);
        final FinalEntityState finalState = context.getFinalEntity(entityChange);

        final String entityId = extractEntityId(entityChange, currentState);

        final Collection<EntityFieldValue> mandatoryFieldValues = mandatoryFieldValuesGenerator.generate(finalState);

        final Collection<FieldAuditRecord<E>> fieldRecords = fieldChangesGenerator.generate(currentState, finalState);

        return new AuditRecord.Builder<E>()
            .withEntityType(entityTypeName)
            .withEntityId(entityId)
            .withMandatoryFieldValues(mandatoryFieldValues)
            .withOperator(entityChange.getChangeOperation())
            .withFieldRecords(fieldRecords)
            .withChildRecords(childRecords)
            .build();
    }

    private String extractEntityId(final EntityChange<E> entityChange,
                                   final CurrentEntityState currentState) {
        return entityIdExtractor.extract(entityChange, currentState)
                                .orElseThrow(() -> new IllegalStateException("Could not extract the entity id for entity type '" + entityChange.getEntityType() + "' " +
                                                                                 "from either the EntityChange or the CurrentEntityState, so the audit record cannot be generated."));
    }

    @VisibleForTesting
    @Override
    public String getEntityTypeName() {
        return entityTypeName;
    }
}