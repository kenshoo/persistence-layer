package com.kenshoo.pl.entity.internal.audit;

import com.google.common.annotations.VisibleForTesting;
import com.kenshoo.pl.entity.FieldValue;
import com.kenshoo.pl.entity.FinalEntityState;
import com.kenshoo.pl.entity.TransientEntityProperty;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Collection;
import java.util.stream.Stream;

import static java.util.Map.entry;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toUnmodifiableList;

public class AuditMandatoryFieldValuesGenerator {

    private final Collection<AuditedField<?, ?>> mandatoryFields;
    private final Collection<? extends TransientEntityProperty<?, ?>> mandatoryProperties;
    private final AuditFieldValueResolver auditFieldValueResolver;

    public AuditMandatoryFieldValuesGenerator(final AuditedEntityType<?> auditedEntityType) {
        this(auditedEntityType, AuditFieldValueResolver.INSTANCE);
    }

    @VisibleForTesting
    AuditMandatoryFieldValuesGenerator(final AuditedEntityType<?> auditedEntityType,
                                       final AuditFieldValueResolver auditFieldValueResolver) {
        requireNonNull(auditedEntityType, "An AuditedEntityType must be provided");

        this.mandatoryFields = auditedEntityType.getMandatoryFields().collect(toUnmodifiableList());
        this.mandatoryProperties = auditedEntityType.getMandatoryProperties();
        this.auditFieldValueResolver = auditFieldValueResolver;
    }

    Collection<FieldValue> generate(final FinalEntityState finalState) {
        requireNonNull(finalState, "finalState is required");
        return Stream.concat(generateFieldValues(finalState), generatePropertyValues(finalState))
            .collect(toUnmodifiableList());
    }

    private Stream<FieldValue> generateFieldValues(FinalEntityState finalState) {
        return mandatoryFields.stream()
                .map(field -> ImmutablePair.of(field, auditFieldValueResolver.resolveToString(field, finalState)))
                .filter(pair -> pair.getValue().isNotNull())
                .map(pair -> new FieldValue(pair.getKey().getName(), pair.getValue().get()));
    }

    private Stream<FieldValue> generatePropertyValues(FinalEntityState finalState) {
        return mandatoryProperties.stream()
                .map(property -> entry(property, finalState.get(property)))
                .filter(entry -> entry.getValue().isPresent())
                .map(entry -> new FieldValue(entry.getKey().getName(), String.valueOf(entry.getValue().get())));
    }
}
