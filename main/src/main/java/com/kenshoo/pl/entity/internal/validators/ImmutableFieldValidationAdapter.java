package com.kenshoo.pl.entity.internal.validators;

import com.google.common.collect.ImmutableMap;
import com.kenshoo.pl.entity.ChangeOperation;
import com.kenshoo.pl.entity.Entity;
import com.kenshoo.pl.entity.EntityChange;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.SupportedChangeOperation;
import com.kenshoo.pl.entity.ValidationError;
import com.kenshoo.pl.entity.spi.ImmutableFieldValidator;

import java.util.stream.Stream;

public class ImmutableFieldValidationAdapter<E extends EntityType<E>, T> implements EntityChangeValidator<E> {

    private final ImmutableFieldValidator<E, T> validator;

    public ImmutableFieldValidationAdapter(ImmutableFieldValidator<E, T> validator) {
        this.validator = validator;
    }

    @Override
    public Stream<EntityField<E, ?>> getValidatedFields() {
        return Stream.of(validator.immutableField());
    }

    @Override
    public SupportedChangeOperation getSupportedChangeOperation() {
        return SupportedChangeOperation.UPDATE;
    }

    @Override
    public Stream<? extends EntityField<?, ?>> getFieldsToFetch(ChangeOperation changeOperation) {
        Stream<EntityField<?, ?>> fetchFields = validator.fetchFields();
        if(fetchFields != null) {
            return Stream.concat(Stream.of(validator.immutableField()), validator.fetchFields());
        } else {
           return Stream.of(validator.immutableField());
        }
    }

    @Override
    public ValidationError validate(EntityChange<E> entityChange, Entity entity, ChangeOperation changeOperation) {
        if (entityChange.isFieldChanged(validator.immutableField()) && validator.immutableWhen().test(entity)) {
            return new ValidationError(validator.getErrorCode(), validator.immutableField(), ImmutableMap.of("field", validator.immutableField().toString()));
        }
        return null;

    }
}
