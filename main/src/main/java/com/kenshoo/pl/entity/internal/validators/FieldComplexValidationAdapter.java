package com.kenshoo.pl.entity.internal.validators;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.spi.FieldComplexValidator;

import java.util.stream.Stream;

public class FieldComplexValidationAdapter<E extends EntityType<E>, T> implements ChangeValidatorAdapter<E> {

    private final FieldComplexValidator<E, T> validator;
    private final ValidationTrigger<E> trigger;

    public FieldComplexValidationAdapter(FieldComplexValidator<E, T> validator) {
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
    public Stream<? extends EntityField<?, ?>> fieldsToFetch() {
        return validator.fetchFields();
    }

    @Override
    public ValidationError validate(EntityChange<E> entityChange, CurrentEntityState currentState, FinalEntityState finalState) {
        if (entityChange.isFieldChanged(validator.validatedField())) {
            return validator.validate(entityChange.get(validator.validatedField()), currentState);
        } else {
            return null;
        }
    }
}
