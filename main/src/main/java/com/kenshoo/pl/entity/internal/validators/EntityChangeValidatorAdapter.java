package com.kenshoo.pl.entity.internal.validators;

import com.kenshoo.pl.entity.*;

import java.util.stream.Stream;

public class EntityChangeValidatorAdapter<E extends EntityType<E>> implements ChangeValidatorAdapter<E> {

    private final EntityChangeValidator<E> validator;
    private final ValidationTrigger<E> trigger;

    public EntityChangeValidatorAdapter(EntityChangeValidator<E> validator) {
        this.validator = validator;
        this.trigger = new AnyFieldsTrigger<>(validator.validatedFields());
    }

    @Override
    public ValidationTrigger<E> trigger() {
        return trigger;
    }

    @Override
    public SupportedChangeOperation getSupportedChangeOperation() {
        return validator.getSupportedChangeOperation();
    }

    @Override
    public Stream<? extends EntityField<?, ?>> fetchFields() {
        return validator.fetchFields();
    }

    @Override
    public ValidationError validate(EntityChange<E> entityChange, CurrentEntityState currentState) {
        return validator.validate(entityChange, currentState);
    }
}
