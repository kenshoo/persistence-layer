package com.kenshoo.pl.entity.validators;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.spi.ChangesValidator;

import java.util.Collection;

/**
 * A validator that force given a child in command
 *
 * @param <E>     entity type
 * @param <CHILD> entity child type
 */
public class RequiredAtLeastOneChildValidator<E extends EntityType<E>, CHILD extends EntityType<CHILD>> implements ChangesValidator<E> {

    private final CHILD childType;

    public RequiredAtLeastOneChildValidator(CHILD childType) {
        this.childType = childType;
    }

    @Override
    public void validate(Collection<? extends EntityChange<E>> entityChanges, ChangeOperation changeOperation, ChangeContext changeContext) {
        entityChanges.forEach(entityChange -> {
            long childrenAmount = entityChange.getChildren(childType).count();
            if (childrenAmount == 0) {
                changeContext.addValidationError(entityChange,
                        new ValidationError(String.format("At least one %s is required.", childType.getName())));
            }
        });
    }

    @Override
    public SupportedChangeOperation getSupportedChangeOperation() {
        return SupportedChangeOperation.CREATE;
    }
}