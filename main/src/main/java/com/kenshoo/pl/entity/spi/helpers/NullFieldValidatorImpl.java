package com.kenshoo.pl.entity.spi.helpers;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.ValidationError;
import com.kenshoo.pl.entity.spi.FieldValidator;

public class NullFieldValidatorImpl<E extends EntityType<E>, T> implements FieldValidator<E, T> {

    private final EntityField<E, T> entityField;
    private final String errorCode;

    public NullFieldValidatorImpl(EntityField<E, T> entityField, String errorCode) {
        this.entityField = entityField;
        this.errorCode = errorCode;
    }

    @Override
    public EntityField<E, T> validatedField() {
        return entityField;
    }

    @Override
    public ValidationError validate(T fieldValue) {
        if(fieldValue == null) {
            return new ValidationError(errorCode, entityField);
        } else {
            return null;
        }
    }
}
