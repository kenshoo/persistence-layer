package com.kenshoo.pl.entity.internal.audit;

import com.google.common.annotations.VisibleForTesting;
import com.kenshoo.pl.entity.FieldValue;
import com.kenshoo.pl.entity.FinalEntityState;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Collection;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.jooq.lambda.Seq.seq;

public class AuditMandatoryFieldValuesGenerator {

    private final Collection<AuditedField<?, ?>> mandatoryFields;
    private final AuditFieldValueResolver auditFieldValueResolver;

    public AuditMandatoryFieldValuesGenerator(final Stream<? extends AuditedField<?, ?>> mandatoryFields) {
        this(mandatoryFields, AuditFieldValueResolver.INSTANCE);
    }

    @VisibleForTesting
    AuditMandatoryFieldValuesGenerator(final Stream<? extends AuditedField<?, ?>> mandatoryFields,
                                       final AuditFieldValueResolver auditFieldValueResolver) {
        requireNonNull(mandatoryFields, "mandatoryFields must not be null (can be empty)");
        this.mandatoryFields = mandatoryFields.collect(toList());
        this.auditFieldValueResolver = auditFieldValueResolver;
    }

    Collection<FieldValue> generate(final FinalEntityState finalState) {
        requireNonNull(finalState, "finalState is required");
        return seq(mandatoryFields)
            .map(field -> ImmutablePair.of(field, auditFieldValueResolver.resolveToString(field, finalState)))
            .filter(pair -> pair.getValue().isNotNull())
            .map(pair -> new FieldValue(pair.getKey().getName(), pair.getValue().get()))
            .collect(toList());
    }
}
