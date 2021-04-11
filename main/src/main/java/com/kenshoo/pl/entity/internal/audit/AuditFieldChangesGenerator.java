package com.kenshoo.pl.entity.internal.audit;

import com.google.common.annotations.VisibleForTesting;
import com.kenshoo.pl.entity.CurrentEntityState;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.FinalEntityState;
import com.kenshoo.pl.entity.audit.FieldAuditRecord;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.jooq.lambda.Seq.seq;

public class AuditFieldChangesGenerator<E extends EntityType<E>> {

    private final Collection<EntityField<E, ?>> onChangeFields;
    private final AuditFieldChangeGenerator singleGenerator;

    public AuditFieldChangesGenerator(final Stream<? extends EntityField<E, ?>> onChangeFields) {
        this(onChangeFields, new AuditFieldChangeGenerator());
    }

    @VisibleForTesting
    AuditFieldChangesGenerator(final Stream<? extends EntityField<E, ?>> onChangeFields,
                               final AuditFieldChangeGenerator singleGenerator) {
        requireNonNull(onChangeFields, "onChangeFields must not be null (can be empty)");
        this.onChangeFields = onChangeFields.collect(toList());
        this.singleGenerator = singleGenerator;
    }

    Collection<FieldAuditRecord> generate(final CurrentEntityState currentState,
                                          final FinalEntityState finalState) {
        return seq(onChangeFields)
            .map(field -> singleGenerator.generate(currentState, finalState, field))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(toList());
    }
}
