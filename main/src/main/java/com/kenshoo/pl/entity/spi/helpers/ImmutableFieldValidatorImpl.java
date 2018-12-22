package com.kenshoo.pl.entity.spi.helpers;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.spi.ImmutableFieldValidator;

public class ImmutableFieldValidatorImpl<E extends EntityType<E>, T> implements ImmutableFieldValidator<E, T> {

    private final EntityField<E, T> entityField;
    private final String errorCode;

    public ImmutableFieldValidatorImpl(EntityField<E, T> entityField, String errorCode) {
        this.entityField = entityField;
        this.errorCode = errorCode;
    }

    @Override
    public EntityField<E, T> immutableField() {
        return entityField;
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }
}
