package com.kenshoo.pl.entity.internal.validators;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.spi.ImmutableParentFieldsValidator;

import java.util.stream.Stream;

public class ImmutableParentFieldsValidatorAdapter<E extends EntityType<E>> implements EntityChangeValidator<E> {

    private final ImmutableParentFieldsValidator<E> validator;

    public ImmutableParentFieldsValidatorAdapter(ImmutableParentFieldsValidator<E> validator) {
        this.validator = validator;
    }

    @Override
    public Stream<EntityField<E, ?>> validatedFields() {
        return Stream.empty();
    }

    @Override
    public SupportedChangeOperation getSupportedChangeOperation() {
        return SupportedChangeOperation.CREATE_UPDATE_AND_DELETE;
    }

    @Override
    public Stream<? extends EntityField<?, ?>> fetchFields() {
        return validator.parentsFields();
    }

    @Override
    public ValidationError validate(EntityChange<E> entityChange, Entity entity) {
        return validator.immutableWhen().test(entity) ? validator.errorFor(entityChange, entity) : null;
    }
}
