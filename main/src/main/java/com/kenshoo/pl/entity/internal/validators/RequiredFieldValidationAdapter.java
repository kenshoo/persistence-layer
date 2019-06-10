package com.kenshoo.pl.entity.internal.validators;

import com.google.common.collect.ImmutableMap;
import com.kenshoo.pl.entity.ChangeOperation;
import com.kenshoo.pl.entity.Entity;
import com.kenshoo.pl.entity.EntityChange;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.SupportedChangeOperation;
import com.kenshoo.pl.entity.ValidationError;
import com.kenshoo.pl.entity.spi.RequiredFieldValidator;

import java.util.stream.Stream;

public class RequiredFieldValidationAdapter<E extends EntityType<E>, T> implements EntityChangeValidator<E> {

    private final RequiredFieldValidator<E, T> validator;

    public RequiredFieldValidationAdapter(RequiredFieldValidator<E, T> validator) {
        this.validator = validator;
    }

    @Override
    public Stream<EntityField<E, ?>> getValidatedFields() {
        return Stream.of(validator.requiredField());
    }

    @Override
    public SupportedChangeOperation getSupportedChangeOperation() {
        return SupportedChangeOperation.CREATE;
    }

    @Override
    public Stream<? extends EntityField<?, ?>> getFieldsToFetch(ChangeOperation changeOperation) {
        return Stream.empty();
    }

    @Override
    public ValidationError validate(EntityChange<E> entityChange, Entity entity, ChangeOperation changeOperation) {
        if (entityChange.get(validator.requiredField()) == null) {
            return new ValidationError(validator.getErrorCode(), validator.requiredField(), ImmutableMap.of("field", validator.requiredField().toString()));
        }
        return null;
    }
}
