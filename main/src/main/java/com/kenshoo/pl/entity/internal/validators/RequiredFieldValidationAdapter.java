package com.kenshoo.pl.entity.internal.validators;

import com.google.common.collect.ImmutableMap;
import com.kenshoo.pl.entity.CurrentEntityState;
import com.kenshoo.pl.entity.EntityChange;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.SupportedChangeOperation;
import com.kenshoo.pl.entity.ValidationError;
import com.kenshoo.pl.entity.spi.RequiredFieldValidator;

import java.util.stream.Stream;

public class RequiredFieldValidationAdapter<E extends EntityType<E>, T> implements ChangeValidatorAdapter<E> {

    private final RequiredFieldValidator<E, T> validator;

    public RequiredFieldValidationAdapter(RequiredFieldValidator<E, T> validator) {
        this.validator = validator;
    }

    @Override
    public ValidationTrigger<E> trigger() {
        return entityFields -> true;
    }

    @Override
    public SupportedChangeOperation getSupportedChangeOperation() {
        return SupportedChangeOperation.CREATE;
    }

    @Override
    public Stream<? extends EntityField<?, ?>> fieldsToFetch() {
        return validator.fetchFields();
    }

    @Override
    public ValidationError validate(EntityChange<E> entityChange, CurrentEntityState currentState) {
        if (entityChange.get(validator.requiredField()) == null && validator.requireWhen().test(currentState)) {
            return new ValidationError(validator.getErrorCode(), validator.requiredField(), ImmutableMap.of("field", validator.requiredField().toString()));
        }
        return null;
    }
}
