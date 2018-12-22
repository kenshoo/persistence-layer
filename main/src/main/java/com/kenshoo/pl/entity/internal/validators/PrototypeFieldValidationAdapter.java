package com.kenshoo.pl.entity.internal.validators;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.spi.PrototypeFieldValidator;

import java.util.stream.Stream;

public class PrototypeFieldValidationAdapter<E extends EntityType<E>, T> implements EntityChangeValidator<E> {

    private EntityField<E, T> validatedField;
    private final PrototypeFieldValidator<T> prototypeFieldValidator;

    public PrototypeFieldValidationAdapter(EntityField<E, T> validatedField, PrototypeFieldValidator<T> prototypeFieldValidator) {
        this.validatedField = validatedField;
        this.prototypeFieldValidator = prototypeFieldValidator;
    }

    @Override
    public Stream<EntityField<E, ?>> getValidatedFields() {
        return Stream.of(this.validatedField);
    }

    @Override
    public SupportedChangeOperation getSupportedChangeOperation() {
        return SupportedChangeOperation.CREATE_AND_UPDATE;
    }

    @Override
    public Stream<? extends EntityField<?, ?>> getFieldsToFetch(ChangeOperation changeOperation) {
        return Stream.empty();
    }

    @Override
    public ValidationError validate(EntityChange<E> entityChange, Entity entity, ChangeOperation changeOperation) {
        if (entityChange.isFieldChanged(validatedField)) {
            ValidationError error = prototypeFieldValidator.validate(entityChange.get(validatedField));
            return error != null ? new ValidationError(error.getErrorCode(), validatedField, error.getParameters()) : null;
        } else {
            return null;
        }
    }
}
