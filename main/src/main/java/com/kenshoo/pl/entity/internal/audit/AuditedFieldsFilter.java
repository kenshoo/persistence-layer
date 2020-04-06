package com.kenshoo.pl.entity.internal.audit;

import com.google.common.annotations.VisibleForTesting;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;

import java.util.Collection;

import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

public class AuditedFieldsFilter<E extends EntityType<E>> {

    private final AuditedFieldSet<E> completeFieldSet;

    public AuditedFieldsFilter(final AuditedFieldSet<E> completeFieldSet) {
        this.completeFieldSet = requireNonNull(completeFieldSet);
    }

    public AuditedFieldSet<E> filter(final Collection<? extends EntityField<E, ?>> fieldsToUpdate) {
        final Collection<? extends EntityField<E, ?>> adjustedFieldsToUpdate = fieldsToUpdate == null ? emptySet() : fieldsToUpdate;
        return new AuditedFieldSet<>(completeFieldSet.getIdField(),
                                     adjustedFieldsToUpdate.stream()
                                                         .filter(completeFieldSet.getAuditedFields()::contains)
                                                         .collect(toSet()));
    }

    @VisibleForTesting
    AuditedFieldSet<E> getCompleteFieldSet() {
        return completeFieldSet;
    }
}