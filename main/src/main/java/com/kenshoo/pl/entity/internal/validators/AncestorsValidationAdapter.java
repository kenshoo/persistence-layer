package com.kenshoo.pl.entity.internal.validators;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.spi.AncestorsValidator;

import java.util.stream.Stream;

public class AncestorsValidationAdapter<E extends EntityType<E>> implements EntityChangeValidator<E> {

    private final AncestorsValidator<E> validator;

    public AncestorsValidationAdapter(AncestorsValidator<E> validator) {
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
        return validator.ancestorsFields();
    }

    @Override
    public ValidationError validate(EntityChange<E> entityChange, Entity currentState) {
        return validator.ancestorsRestriction().test(currentState) ? validator.errorFor(entityChange, currentState) : null;
    }
}
