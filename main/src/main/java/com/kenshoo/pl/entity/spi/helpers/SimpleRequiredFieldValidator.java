package com.kenshoo.pl.entity.spi.helpers;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.ValidationError;
import com.kenshoo.pl.entity.spi.RequiredFieldValidator;

public class SimpleRequiredFieldValidator<E extends EntityType<E>, T> implements RequiredFieldValidator<E, T> {

    private final EntityField<E, T> entityField;
    private final String errorCode;
    private final ValidationError.IsShowStopper isShowStopper;

    public SimpleRequiredFieldValidator(EntityField<E, T> entityField, String errorCode) {
      this(entityField, errorCode, ValidationError.IsShowStopper.No);
    }

    public SimpleRequiredFieldValidator(EntityField<E, T> entityField, String errorCode, ValidationError.IsShowStopper isShowStopper) {
        this.entityField = entityField;
        this.errorCode = errorCode;
        this.isShowStopper = isShowStopper;
    }


    @Override
    public EntityField<E, T> requiredField() {
        return entityField;
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public ValidationError.IsShowStopper isShowStopper() { return isShowStopper; }
}
