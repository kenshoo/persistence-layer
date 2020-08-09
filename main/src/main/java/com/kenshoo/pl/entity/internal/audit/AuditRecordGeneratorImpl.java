package com.kenshoo.pl.entity.internal.audit;

import com.google.common.annotations.VisibleForTesting;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.audit.AuditRecord;
import com.kenshoo.pl.entity.audit.FieldAuditRecord;
import com.kenshoo.pl.entity.internal.EntityIdExtractor;

import java.util.Collection;
import java.util.Optional;
import java.util.function.BiFunction;

import static com.kenshoo.pl.entity.ChangeOperation.UPDATE;
import static java.util.Objects.requireNonNull;

public class AuditRecordGeneratorImpl<E extends EntityType<E>> implements AuditRecordGenerator<E> {

    private final AuditMandatoryFieldValuesGenerator mandatoryFieldValuesGenerator;
    private final AuditFieldChangesGenerator<E> fieldChangesGenerator;
    private final EntityIdExtractor entityIdExtractor;
    private final FinalEntityStateCreator finalStateCreator;

    public AuditRecordGeneratorImpl(final AuditMandatoryFieldValuesGenerator mandatoryFieldValuesGenerator,
                                    final AuditFieldChangesGenerator<E> fieldChangesGenerator) {
        this(mandatoryFieldValuesGenerator,
             fieldChangesGenerator,
             EntityIdExtractor.INSTANCE,
             FinalEntityState::new);
    }

    @VisibleForTesting
    AuditRecordGeneratorImpl(final AuditMandatoryFieldValuesGenerator mandatoryFieldValuesGenerator,
                             final AuditFieldChangesGenerator<E> fieldChangesGenerator,
                             final EntityIdExtractor entityIdExtractor,
                             final FinalEntityStateCreator finalStateCreator) {
        this.mandatoryFieldValuesGenerator = mandatoryFieldValuesGenerator;
        this.fieldChangesGenerator = fieldChangesGenerator;
        this.entityIdExtractor = entityIdExtractor;
        this.finalStateCreator = finalStateCreator;
    }

    @Override
    public Optional<AuditRecord<E>> generate(final EntityChange<E> entityChange,
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

        final FinalEntityState finalState = finalStateCreator.apply(currentState, entityChange);

        final Collection<EntityFieldValue> mandatoryFieldValues = mandatoryFieldValuesGenerator.generate(finalState);

        final Collection<FieldAuditRecord<E>> fieldRecords = fieldChangesGenerator.generate(currentState, finalState);

        return new AuditRecord.Builder<E>()
            .withEntityType(entityChange.getEntityType())
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

    interface FinalEntityStateCreator extends BiFunction<CurrentEntityState, EntityChange<?>, FinalEntityState> {
    }
}