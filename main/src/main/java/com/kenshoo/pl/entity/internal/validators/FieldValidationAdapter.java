package com.kenshoo.pl.entity.internal.validators;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.spi.FieldValidator;

import java.util.stream.Stream;

public class FieldValidationAdapter<E extends EntityType<E>, T> implements EntityChangeValidator<E> {

    private final FieldValidator<E, T> validator;

    public FieldValidationAdapter(FieldValidator<E, T> validator) {
        this.validator = validator;
    }

    @Override
    public Stream<EntityField<E, ?>> getValidatedFields() {
        return Stream.of(validator.validatedField());
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
        if (entityChange.isFieldChanged(validator.validatedField())) {
            return validator.validate(entityChange.get(validator.validatedField()));
        } else {
            return null;
        }
    }
}
