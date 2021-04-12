package com.kenshoo.pl.entity.internal.audit;

import com.google.common.annotations.VisibleForTesting;
import com.kenshoo.pl.entity.CurrentEntityState;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.FinalEntityState;
import com.kenshoo.pl.entity.audit.FieldAuditRecord;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class AuditFieldChangeGenerator {

    public static final AuditFieldChangeGenerator INSTANCE = new AuditFieldChangeGenerator(AuditFieldValueResolver.INSTANCE);

    private final AuditFieldValueResolver auditFieldValueResolver;

    AuditFieldChangeGenerator() {
        this(AuditFieldValueResolver.INSTANCE);
    }

    @VisibleForTesting
    AuditFieldChangeGenerator(final AuditFieldValueResolver auditFieldValueResolver) {
        // singleton
        this.auditFieldValueResolver = auditFieldValueResolver;
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
        return triptionalCurrentValue.equals(triptionalFinalValue, field::valuesEqual);
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
