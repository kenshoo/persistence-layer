package com.kenshoo.pl.entity.spi.helpers;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.spi.RequiredFieldValidator;

public class RequiredFieldValidatorImpl<E extends EntityType<E>, T> implements RequiredFieldValidator<E, T> {

    private final EntityField<E, T> entityField;
    private final String errorCode;

    public RequiredFieldValidatorImpl(EntityField<E, T> entityField, String errorCode) {
        this.entityField = entityField;
        this.errorCode = errorCode;
    }


    @Override
    public EntityField<E, T> requiredField() {
        return entityField;
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }
}
