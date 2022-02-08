package com.kenshoo.pl.entity.spi;

import com.kenshoo.pl.entity.*;

import java.util.Collection;

/**
 * A validator that force given a child in command
 *
 * @param <E> entity type
 * @param <CHILD> child entity type
 */
public interface RequiredAtLeastOneChildValidator <E extends EntityType<E>, CHILD extends EntityType<CHILD>> extends ChangesValidator<E>{

    CHILD childType();

    @Override
    default void validate(Collection<? extends EntityChange<E>> entityChanges, ChangeOperation changeOperation, ChangeContext changeContext) {
        entityChanges.forEach(entityChange -> {
            long childrenAmount = entityChange.getChildren(childType()).count();
            if(childrenAmount == 0){
                changeContext.addValidationError(entityChange,
                        new ValidationError(String.format("At least one %s is required", childType().getName())));
            }
        });

    }

    @Override
    default SupportedChangeOperation getSupportedChangeOperation() {
        return SupportedChangeOperation.CREATE;
    }
}
