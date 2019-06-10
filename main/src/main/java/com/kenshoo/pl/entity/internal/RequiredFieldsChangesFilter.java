package com.kenshoo.pl.entity.internal;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.kenshoo.pl.entity.ChangeContext;
import com.kenshoo.pl.entity.ChangeEntityCommand;
import com.kenshoo.pl.entity.ChangeOperation;
import com.kenshoo.pl.entity.EntityChange;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.SupportedChangeOperation;
import com.kenshoo.pl.entity.ValidationError;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

public class RequiredFieldsChangesFilter<E extends EntityType<E>> implements ChangesFilter<E> {

    private final Set<EntityField<E, ?>> requiredFields;

    public RequiredFieldsChangesFilter(final Set<EntityField<E, ?>> requiredFields) {
        this.requiredFields = requiredFields;
    }

    @Override
    public <T extends EntityChange<E>> Collection<T> filter(Collection<T> changes, ChangeOperation changeOperation, ChangeContext changeContext) {
        return Collections2.filter(changes, change -> Iterables.all(requiredFields, entityField -> {
            boolean fieldSpecified = change.isFieldChanged(entityField) && change.get(entityField) != null;
            if (!fieldSpecified) {
                changeContext.addValidationError(change,
                        new ValidationError(Errors.FIELD_IS_REQUIRED, entityField, ImmutableMap.of("field", entityField.toString())));
            }
            return fieldSpecified;
        }));
    }

    @Override
    public Stream<EntityField<?, ?>> getRequiredFields(Collection<? extends ChangeEntityCommand<E>> commands, ChangeOperation changeOperation) {
        return Stream.empty();
    }

    @Override
    public SupportedChangeOperation getSupportedChangeOperation() {
        return SupportedChangeOperation.CREATE;
    }
}
