package com.kenshoo.pl.entity.internal.validators;

import com.kenshoo.pl.entity.Entity;
import com.kenshoo.pl.entity.EntityChange;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.SupportedChangeOperation;
import com.kenshoo.pl.entity.ValidationError;
import com.kenshoo.pl.entity.spi.FieldComplexValidator;

import java.util.stream.Stream;

public class FieldComplexValidationAdapter<E extends EntityType<E>, T> implements EntityChangeValidator<E> {

    private final FieldComplexValidator<E, T> validator;

    public FieldComplexValidationAdapter(FieldComplexValidator<E, T> validator) {
        this.validator = validator;
    }

    @Override
    public Stream<EntityField<E, ?>> validatedFields() {
        return Stream.of(validator.validatedField());
    }

    @Override
    public SupportedChangeOperation getSupportedChangeOperation() {
        return SupportedChangeOperation.CREATE_AND_UPDATE;
    }

    @Override
    public Stream<? extends EntityField<?, ?>> fetchFields() {
        return validator.fetchFields();
    }

    @Override
    public ValidationError validate(EntityChange<E> entityChange, Entity currentState) {
        if (entityChange.isFieldChanged(validator.validatedField())) {
            return validator.validate(entityChange.get(validator.validatedField()), currentState);
        } else {
            return null;
        }
    }
}
