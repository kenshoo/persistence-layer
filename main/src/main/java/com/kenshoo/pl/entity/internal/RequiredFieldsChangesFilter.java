package com.kenshoo.pl.entity.internal;

import com.google.common.collect.ImmutableMap;
import com.kenshoo.pl.entity.*;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.kenshoo.pl.entity.SupportedChangeOperation.CREATE;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class RequiredFieldsChangesFilter<E extends EntityType<E>> implements ChangesFilter<E> {

    private final Set<EntityField<E, ?>> requiredFields;

    public RequiredFieldsChangesFilter(final Set<EntityField<E, ?>> requiredFields) {
        this.requiredFields = requiredFields;
    }

    @Override
    public <T extends EntityChange<E>> Collection<T> filter(Collection<T> changes, ChangeOperation changeOperation, ChangeContext context) {

        final List<EntityField<E, ?>> requiredFields = entityType()
                .map(entityType -> only(this.requiredFields, notReferringToParentIn(context.getHierarchy(), entityType)))
                .orElse(emptyList());

        if (requiredFields.isEmpty()) {
            return changes;
        }

        return changes.stream().filter(change -> requiredFields.stream().allMatch(entityField -> {
            boolean fieldSpecified = change.isFieldChanged(entityField) && change.get(entityField) != null;
            if (!fieldSpecified) {
                context.addValidationError(change,
                        new ValidationError(Errors.FIELD_IS_REQUIRED, entityField, ImmutableMap.of("field", entityField.toString())));
            }
            return fieldSpecified;
        })).collect(toList());
    }

    private Predicate<EntityField<E, ?>> notReferringToParentIn(Hierarchy hierarchy, EntityType<E> entityType) {
        return new IsFieldReferringToParent<>(hierarchy, entityType).negate();
    }

    private Optional<EntityType<E>> entityType() {
        return requiredFields.stream().findFirst().map(EntityField::getEntityType);
    }

    private <T> List<T> only(Collection<T> items, Predicate<T> predicate) {
        return items.stream().filter(predicate).collect(toList());
    }

    @Override
    public Stream<? extends EntityField<?, ?>> requiredFields(Collection<? extends EntityField<E, ?>> fieldsToUpdate, ChangeOperation changeOperation) {
        return Stream.empty();
    }

    @Override
    public SupportedChangeOperation getSupportedChangeOperation() {
        return CREATE;
    }
}
