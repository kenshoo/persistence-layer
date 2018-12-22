package com.kenshoo.pl.entity.spi.helpers;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.spi.ChangesValidator;
import com.kenshoo.pl.entity.spi.ParentConditionValidator;

import java.util.Collection;
import java.util.stream.Stream;

public class ParentConditionCompositeValidator<E extends EntityType<E>> implements ChangesValidator<E> {

    private final Multimap<EntityField<?, ?>, ParentConditionValidator<?>> parentValidators = HashMultimap.create();

    public void register(ParentConditionValidator<?> validator) {
        parentValidators.put(validator.parentIdField(), validator);
    }

    @Override
    public void validate(Collection<? extends EntityChange<E>> entityChanges, ChangeOperation changeOperation, final ChangeContext changeContext) {
        for (final EntityField<?, ?> field : parentValidators.keySet()) {
            validateForField(entityChanges, changeContext, field);
        }
    }

    @Override
    public Stream<EntityField<?, ?>> getRequiredFields(Collection<? extends ChangeEntityCommand<E>> commands, ChangeOperation changeOperation) {
        return parentValidators.values().stream().map(ParentConditionValidator::parentIdField);
    }

    private <T> void validateForField(Collection<? extends EntityChange<E>> entityChanges, final ChangeContext changeContext, final EntityField<?, T> field) {
        //noinspection unchecked
        Collection<ParentConditionValidator<T>> validators = (Collection<ParentConditionValidator<T>>) (Collection) parentValidators.get(field);
        if (!validators.isEmpty()) {
            validateEntityChanges(entityChanges, changeContext, field, validators);
        }
    }

    private <T> void validateEntityChanges(Collection<? extends EntityChange<E>> entityChanges, final ChangeContext changeContext, final EntityField<?, T> field, Collection<ParentConditionValidator<T>> validators) {
        ImmutableListMultimap<T, ? extends EntityChange<E>> changesByParentId =
                Multimaps.index(entityChanges, entityChange -> changeContext.getEntity(entityChange).get(field));

        for (T parentId : changesByParentId.keySet()) {
            for (ParentConditionValidator<T> validator : validators) {
                ValidationError error = validator.validate(parentId);
                if (error != null) {
                    for (EntityChange<E> entityChange : changesByParentId.get(parentId)) {
                        changeContext.addValidationError(entityChange, error);
                    }
                }
            }
        }
    }
}
