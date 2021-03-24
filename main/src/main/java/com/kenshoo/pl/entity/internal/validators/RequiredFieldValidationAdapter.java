package com.kenshoo.pl.entity.internal.validators;

import com.google.common.collect.ImmutableMap;
import com.kenshoo.pl.entity.*;
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
        if (isFieldNotSpecified(entityChange) && validator.requireWhen().test(currentState)) {
            return new ValidationError(validator.getErrorCode(), validator.requiredField(), ImmutableMap.of("field", validator.requiredField().toString()), validator.showStopper());
        }
        return null;
    }

    private boolean isFieldNotSpecified(EntityChange<E> entityChange) {
        return  entityChange.safeGet(validator.requiredField()).isNullOrAbsent();
    }
}
