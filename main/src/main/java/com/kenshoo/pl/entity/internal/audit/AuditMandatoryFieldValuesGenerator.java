package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.pl.entity.FinalEntityState;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.stream.Stream;

import static java.util.Map.entry;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.jooq.lambda.Seq.seq;

public class AuditMandatoryFieldValuesGenerator {

    private final Collection<AuditedField<?, ?>> mandatoryFields;

    public AuditMandatoryFieldValuesGenerator(final Stream<? extends AuditedField<?, ?>> mandatoryFields) {
        requireNonNull(mandatoryFields, "mandatoryFields must not be null (can be empty)");
        this.mandatoryFields = mandatoryFields.collect(toList());
    }

    Collection<Entry<String, ?>> generate(final FinalEntityState finalState) {
        requireNonNull(finalState, "finalState is required");
        return seq(mandatoryFields)
            .map(field -> ImmutablePair.of(field, field.getValue(finalState)))
            .filter(pair -> pair.getValue().isNotNull())
            .map(pair -> entry(pair.getKey().getName(), pair.getValue().get()))
            .collect(toList());
    }
}
