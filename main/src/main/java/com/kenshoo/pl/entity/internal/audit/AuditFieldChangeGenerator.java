package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.pl.entity.CurrentEntityState;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.FinalEntityState;
import com.kenshoo.pl.entity.audit.FieldAuditRecord;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

class AuditFieldChangeGenerator {

    static final AuditFieldChangeGenerator INSTANCE = new AuditFieldChangeGenerator();

    <E extends EntityType<E>> Optional<FieldAuditRecord<E>> generate(final CurrentEntityState currentState,
                                                                     final FinalEntityState finalState,
                                                                     final EntityField<E, ?> field) {
        requireNonNull(currentState, "A current state is required");
        requireNonNull(finalState, "A final state is required");
        requireNonNull(field, "A field is required");

        return Optional.of(field)
                       .filter(f -> fieldWasChanged(currentState, finalState, f))
                       .map(f -> buildFieldRecord(currentState, finalState, f));
    }

    private <E extends EntityType<E>, T> boolean fieldWasChanged(final CurrentEntityState currentState,
                                                                 final FinalEntityState finalState,
                                                                 final EntityField<E, T> field) {
        return !fieldStayedTheSame(currentState, finalState, field);
    }

    private <E extends EntityType<E>, T> boolean fieldStayedTheSame(final CurrentEntityState currentState,
                                                                    final FinalEntityState finalState,
                                                                    final EntityField<E, T> field) {
        return currentState.safeGet(field).equals(finalState.safeGet(field), field::valuesEqual);
    }

    private <E extends EntityType<E>> FieldAuditRecord<E> buildFieldRecord(final CurrentEntityState currentState,
                                                                           final FinalEntityState finalState,
                                                                           final EntityField<E, ?> field) {
        final FieldAuditRecord.Builder<E> fieldRecordBuilder = FieldAuditRecord.builder(field);
        currentState.safeGet(field).ifNotNull(fieldRecordBuilder::oldValue);
        finalState.safeGet(field).ifNotNull(fieldRecordBuilder::newValue);
        return fieldRecordBuilder.build();
    }
}
