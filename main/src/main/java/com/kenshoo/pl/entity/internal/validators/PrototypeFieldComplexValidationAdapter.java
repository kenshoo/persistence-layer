package com.kenshoo.pl.entity.internal.validators;

import com.kenshoo.pl.entity.Entity;
import com.kenshoo.pl.entity.EntityChange;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.SupportedChangeOperation;
import com.kenshoo.pl.entity.ValidationError;
import com.kenshoo.pl.entity.spi.PrototypeFieldComplexValidator;

import java.util.stream.Stream;

public class PrototypeFieldComplexValidationAdapter<E extends EntityType<E>, T> implements EntityChangeValidator<E> {

    private final EntityField<E, T> validatedField;
    private final PrototypeFieldComplexValidator<T> prototypeFieldValidator;

    public PrototypeFieldComplexValidationAdapter(EntityField<E, T> validatedField, PrototypeFieldComplexValidator<T> prototypeFieldValidator) {
        this.validatedField = validatedField;
        this.prototypeFieldValidator = prototypeFieldValidator;
    }

    @Override
    public Stream<EntityField<E, ?>> validatedFields() {
        return Stream.of(validatedField);
    }

    @Override
    public SupportedChangeOperation getSupportedChangeOperation() {
        return SupportedChangeOperation.CREATE_AND_UPDATE;
    }

    @Override
    public Stream<? extends EntityField<?, ?>> fetchFields() {
        return prototypeFieldValidator.fetchFields();
    }

    @Override
    public ValidationError validate(EntityChange<E> entityChange, Entity entity) {
        if (entityChange.isFieldChanged(validatedField)) {
            return prototypeFieldValidator.validate(entityChange.get(validatedField), entity);
        } else {
            return null;
        }
    }
}
