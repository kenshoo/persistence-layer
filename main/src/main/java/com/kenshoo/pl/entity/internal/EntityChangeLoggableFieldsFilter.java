package com.kenshoo.pl.entity.internal;

import com.google.common.annotations.VisibleForTesting;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;

import java.util.Collection;

import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

public class EntityChangeLoggableFieldsFilter<E extends EntityType<E>> {

    private final EntityChangeLoggableFieldSet<E> completeLoggableFieldSet;

    public EntityChangeLoggableFieldsFilter(final EntityChangeLoggableFieldSet<E> completeLoggableFieldSet) {
        this.completeLoggableFieldSet = requireNonNull(completeLoggableFieldSet);
    }

    public EntityChangeLoggableFieldSet<E> filter(final Collection<? extends EntityField<E, ?>> fieldsToUpdate) {
        final Collection<? extends EntityField<E, ?>> adjustedFieldsToUpdate = fieldsToUpdate == null ? emptySet() : fieldsToUpdate;
        return new EntityChangeLoggableFieldSet<>(completeLoggableFieldSet.getIdField(),
                                                  adjustedFieldsToUpdate.stream()
                                                                        .filter(completeLoggableFieldSet.getAdditionalFields()::contains)
                                                                        .collect(toSet()));
    }

    @VisibleForTesting
    EntityChangeLoggableFieldSet<E> getCompleteLoggableFieldSet() {
        return completeLoggableFieldSet;
    }
}