package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.pl.entity.CurrentEntityState;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.FinalEntityState;
import com.kenshoo.pl.entity.audit.FieldAuditRecord;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class AuditFieldChangeGenerator {

    private final AuditFieldValueResolver auditFieldValueResolver;

    AuditFieldChangeGenerator(final AuditFieldValueResolver auditFieldValueResolver) {
        this.auditFieldValueResolver = requireNonNull(auditFieldValueResolver, "auditFieldValueResolver is required");
    }

    <E extends EntityType<E>> Optional<FieldAuditRecord> generate(final CurrentEntityState currentState,
                                                                  final FinalEntityState finalState,
                                                                  final AuditedField<E, ?> field) {
        requireNonNull(currentState, "A current state is required");
        requireNonNull(finalState, "A final state is required");
        requireNonNull(field, "A field is required");

        return Optional.of(field)
                       .filter(f -> fieldWasChanged(currentState, finalState, f))
                       .map(f -> buildFieldRecord(currentState, finalState, f));
    }

    private <E extends EntityType<E>, T> boolean fieldWasChanged(final CurrentEntityState currentState,
                                                                 final FinalEntityState finalState,
                                                                 final AuditedField<E, T> field) {
        return !fieldStayedTheSame(currentState, finalState, field);
    }

    private <E extends EntityType<E>, T> boolean fieldStayedTheSame(final CurrentEntityState currentState,
                                                                    final FinalEntityState finalState,
                                                                    final AuditedField<E, T> field) {
        final var triptionalCurrentValue = auditFieldValueResolver.resolve(field, currentState);
        final var triptionalFinalValue = auditFieldValueResolver.resolve(field, finalState);
        return triptionalCurrentValue.equalsAsOptional(triptionalFinalValue, field::valuesEqual);
    }

    private <E extends EntityType<E>> FieldAuditRecord buildFieldRecord(final CurrentEntityState currentState,
                                                                        final FinalEntityState finalState,
                                                                        final AuditedField<E, ?> field) {
        final FieldAuditRecord.Builder fieldRecordBuilder = FieldAuditRecord.builder(field.getName());
        auditFieldValueResolver.resolveToString(field, currentState).ifNotNull(fieldRecordBuilder::oldValue);
        auditFieldValueResolver.resolveToString(field, finalState).ifNotNull(fieldRecordBuilder::newValue);
        return fieldRecordBuilder.build();
    }
}
