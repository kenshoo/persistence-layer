package com.kenshoo.pl.entity.internal.validators;

import com.kenshoo.pl.entity.CurrentEntityState;
import com.kenshoo.pl.entity.EntityChange;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.SupportedChangeOperation;
import com.kenshoo.pl.entity.ValidationError;
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
    public Stream<? extends EntityField<?, ?>> fetchFields() {
        return Stream.empty();
    }

    @Override
    public ValidationError validate(EntityChange<E> entityChange, CurrentEntityState currentState) {
        if (entityChange.isFieldChanged(validatedField)) {
            ValidationError error = prototypeFieldValidator.validate(entityChange.get(validatedField));
            return error != null ? new ValidationError(error.getErrorCode(), validatedField, error.getParameters()) : null;
        } else {
            return null;
        }
    }
}
