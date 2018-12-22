package com.kenshoo.pl.entity.spi.helpers;

import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.internal.validators.EntityChangeValidator;
import com.kenshoo.pl.entity.spi.ChangeValidator;
import com.kenshoo.pl.entity.spi.ChangesValidator;
import com.kenshoo.pl.entity.spi.ParentConditionValidator;

import java.util.Collection;

public class CompoundChangesValidatorFactory {

    public static <E extends EntityType<E>> ChangesValidator<E> buildChangesValidator(E entityType, Collection<?> validators) {
        ParentConditionCompositeValidator<E> parentConditionsValidator = new ParentConditionCompositeValidator<>();
        EntityChangeCompositeValidator<E> entityChangeValidator = new EntityChangeCompositeValidator<>();
        CompoundChangesValidator<E> compoundChangesValidator = new CompoundChangesValidator<>();
        compoundChangesValidator.register(parentConditionsValidator);
        compoundChangesValidator.register(entityChangeValidator);

        for (Object validator : validators) {
            if (validator instanceof ChangeValidator) {
                entityChangeValidator.register(entityType, (ChangeValidator) validator);
            } else if(validator instanceof EntityChangeValidator) {
                //noinspection unchecked
                entityChangeValidator.register((EntityChangeValidator<E>)validator);
            } else if (validator instanceof ParentConditionValidator) {
                parentConditionsValidator.register((ParentConditionValidator) validator);
            } else if (validator instanceof ChangesValidator) {
                //noinspection unchecked
                compoundChangesValidator.register((ChangesValidator<E>) validator);
            }
        }
        return compoundChangesValidator;
    }

}
