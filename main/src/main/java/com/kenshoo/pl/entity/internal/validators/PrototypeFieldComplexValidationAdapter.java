package com.kenshoo.pl.entity.internal.validators;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.spi.PrototypeFieldComplexValidator;
import java.util.stream.Stream;

public class PrototypeFieldComplexValidationAdapter<E extends EntityType<E>, T> implements ChangeValidatorAdapter<E> {

    private final EntityField<E, T> validatedField;
    private final PrototypeFieldComplexValidator<T> prototypeFieldValidator;
    private final ValidationTrigger<E> trigger;

    public PrototypeFieldComplexValidationAdapter(EntityField<E, T> validatedField, PrototypeFieldComplexValidator<T> prototypeFieldValidator) {
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
        return prototypeFieldValidator.fetchFields();
    }

    @Override
    public ValidationError validate(EntityChange<E> entityChange, CurrentEntityState currentState,  FinalEntityState finalState) {
        if (entityChange.isFieldChanged(validatedField) && prototypeFieldValidator.validateWhen().test(currentState)) {
            return prototypeFieldValidator.validate(entityChange.get(validatedField), currentState);
        } else {
            return null;
        }
    }
}
