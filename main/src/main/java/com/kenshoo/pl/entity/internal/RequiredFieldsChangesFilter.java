package com.kenshoo.pl.entity.internal;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.kenshoo.pl.entity.*;

import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class RequiredFieldsChangesFilter<E extends EntityType<E>> implements ChangesFilter<E> {

    private final Set<EntityField<E, ?>> requiredFields;

    public RequiredFieldsChangesFilter(final Set<EntityField<E, ?>> requiredFields) {
        this.requiredFields = requiredFields;
    }

    @Override
    public <T extends ChangeEntityCommand<E>> Collection<T> filter(Collection<T> changes, ChangeOperation changeOperation, ChangeContext changeContext) {
        Predicate<EntityField<E, ?>> isReferringToParentCommand = IsFieldReferringToParentCommand.of(changes);
        return Collections2.filter(changes, change -> requiredFields.stream().allMatch(entityField -> {
            boolean fieldSpecified = change.isFieldChanged(entityField) && change.get(entityField) != null;
            boolean isValid = fieldSpecified || isReferringToParentCommand.test(entityField);
            if (!isValid) {
                changeContext.addValidationError(change,
                        new ValidationError(Errors.FIELD_IS_REQUIRED, entityField, ImmutableMap.of("field", entityField.toString())));
            }
            return isValid;
        }));
    }

    @Override
    public Stream<? extends EntityField<?, ?>> requiredFields(Collection<? extends EntityField<E, ?>> fieldsToUpdate, ChangeOperation changeOperation) {
        return Stream.empty();
    }

    @Override
    public SupportedChangeOperation getSupportedChangeOperation() {
        return SupportedChangeOperation.CREATE;
    }
}
