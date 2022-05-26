package com.kenshoo.pl.entity.internal.audit;

import com.google.common.annotations.VisibleForTesting;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.audit.AuditRecord;
import com.kenshoo.pl.entity.audit.FieldAuditRecord;
import com.kenshoo.pl.entity.internal.EntityIdExtractor;

import java.util.Collection;
import java.util.Optional;

import static com.kenshoo.pl.entity.ChangeOperation.UPDATE;
import static com.kenshoo.pl.entity.audit.AuditProperties.ENTITY_CHANGE_DESCRIPTION;
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
    public Optional<AuditRecord> generate(final EntityChange<E> entityChange,
                                          final ChangeContext context,
                                          final Collection<? extends AuditRecord> childRecords) {
        requireNonNull(entityChange, "entityChange is required");

        final AuditRecord auditRecord = generateInner(entityChange,
                                                      context,
                                                      childRecords);

        if (entityChange.getChangeOperation() == UPDATE && auditRecord.hasNoChanges()) {
            return Optional.empty();
        }
        return Optional.of(auditRecord);
    }

    private AuditRecord generateInner(final EntityChange<E> entityChange,
                                      final ChangeContext context,
                                      final Collection<? extends AuditRecord> childRecords) {
        requireNonNull(context, "context is required");

        final CurrentEntityState currentState = context.getEntity(entityChange);
        final FinalEntityState finalState = context.getFinalEntity(entityChange);

        final Optional<String> maybeEntityId = entityIdExtractor.extract(entityChange, currentState);

        final Collection<? extends FieldValue> mandatoryFieldValues = mandatoryFieldValuesGenerator.generate(finalState);

        final Optional<String> maybeEntityChangeDescription = entityChange.get(ENTITY_CHANGE_DESCRIPTION);

        final Collection<FieldAuditRecord> fieldRecords = fieldChangesGenerator.generate(currentState, finalState);

        final var auditRecordBuilder = new AuditRecord.Builder()
            .withEntityType(entityTypeName)
            .withMandatoryFieldValues(mandatoryFieldValues)
            .withOperator(entityChange.getChangeOperation())
            .withFieldRecords(fieldRecords)
            .withChildRecords(childRecords);
        maybeEntityId.ifPresent(auditRecordBuilder::withEntityId);
        maybeEntityChangeDescription.ifPresent(auditRecordBuilder::withEntityChangeDescription);
        return auditRecordBuilder.build();
    }

    @VisibleForTesting
    @Override
    public String getEntityTypeName() {
        return entityTypeName;
    }
}