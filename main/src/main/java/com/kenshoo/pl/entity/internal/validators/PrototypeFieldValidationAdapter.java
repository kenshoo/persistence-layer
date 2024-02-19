package com.kenshoo.pl.entity.internal.validators;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.spi.PrototypeFieldValidator;

import java.util.stream.Stream;

public class PrototypeFieldValidationAdapter<E extends EntityType<E>, T> implements ChangeValidatorAdapter<E> {

    private final EntityField<E, T> validatedField;
    private final PrototypeFieldValidator<T> prototypeFieldValidator;
    private final ValidationTrigger<E> trigger;

    public PrototypeFieldValidationAdapter(EntityField<E, T> validatedField, PrototypeFieldValidator<T> prototypeFieldValidator) {
        this.validatedField = validatedField;
        this.prototypeFieldValidator = prototypeFieldValidator;
        this.trigger = new FieldTrigger<>(validatedField);
    }


    @Override
    public ValidationTrigger<E> trigger() {
        return trigger;
    }

    @Override
    public SupportedChangeOperation getSupportedChangeOperation() {
        return SupportedChangeOperation.CREATE_AND_UPDATE;
    }

    @Override
    public Stream<? extends EntityField<?, ?>> fieldsToFetch() {
        return Stream.empty();
    }

    @Override
    public ValidationError validate(EntityChange<E> entityChange, CurrentEntityState currentState, FinalEntityState finalState) {
        if (entityChange.isFieldChanged(validatedField)) {
            ValidationError error = prototypeFieldValidator.validate(entityChange.get(validatedField));
            return error != null ? new ValidationError(error.getErrorCode(), validatedField, error.getParameters()) : null;
        } else {
            return null;
        }
    }
}
