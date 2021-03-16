package com.kenshoo.pl.entity.spi.helpers;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.spi.RequiredFieldValidator;

public class SimpleRequiredFieldValidator<E extends EntityType<E>, T> implements RequiredFieldValidator<E, T> {

    private final EntityField<E, T> entityField;
    private final String errorCode;
    private final boolean isShowStopper;

    public SimpleRequiredFieldValidator(EntityField<E, T> entityField, String errorCode) {
      this(entityField, errorCode, false);
    }

    public SimpleRequiredFieldValidator(EntityField<E, T> entityField, String errorCode, boolean isShowStopper) {
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
    public boolean isShowStopper() { return isShowStopper; }
}
