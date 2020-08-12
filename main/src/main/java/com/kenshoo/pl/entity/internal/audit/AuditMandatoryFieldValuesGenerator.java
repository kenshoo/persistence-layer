package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityFieldValue;
import com.kenshoo.pl.entity.FinalEntityState;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Collection;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.jooq.lambda.Seq.seq;

public class AuditMandatoryFieldValuesGenerator {

    private final Collection<EntityField<?, ?>> mandatoryFields;

    public AuditMandatoryFieldValuesGenerator(final Stream<? extends EntityField<?, ?>> mandatoryFields) {
        requireNonNull(mandatoryFields, "mandatoryFields must not be null (can be empty)");
        this.mandatoryFields = mandatoryFields.collect(toList());
    }

    Collection<EntityFieldValue> generate(final FinalEntityState finalState) {
        requireNonNull(finalState, "finalState is required");
        return seq(mandatoryFields)
            .map(field -> ImmutablePair.of(field, finalState.safeGet(field)))
            .filter(pair -> pair.getValue().isNotNull())
            .map(pair -> new EntityFieldValue(pair.getKey(), pair.getValue().get()))
            .collect(toList());
    }
}
