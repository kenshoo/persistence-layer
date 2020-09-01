package com.kenshoo.pl.entity.internal.validators;

import com.kenshoo.pl.entity.CurrentEntityState;
import com.kenshoo.pl.entity.EntityChange;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.SupportedChangeOperation;
import com.kenshoo.pl.entity.ValidationError;
import com.kenshoo.pl.entity.spi.FieldValidator;

import java.util.stream.Stream;

public class FieldValidationAdapter<E extends EntityType<E>, T> implements ChangeValidatorAdapter<E> {

    private final FieldValidator<E, T> validator;
    private final ValidationTrigger<E> trigger;

    public FieldValidationAdapter(FieldValidator<E, T> validator) {
        this.validator = validator;
        this.trigger = new FieldTrigger<>(validator.validatedField());
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
        if (entityChange.isFieldChanged(validator.validatedField())) {
            return validator.validate(entityChange.get(validator.validatedField()));
        } else {
            return null;
        }
    }
}
