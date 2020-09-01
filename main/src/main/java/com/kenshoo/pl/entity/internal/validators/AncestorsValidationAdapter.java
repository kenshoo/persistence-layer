package com.kenshoo.pl.entity.internal.validators;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.spi.AncestorsValidator;

import java.util.stream.Stream;

public class AncestorsValidationAdapter<E extends EntityType<E>> implements ChangeValidatorAdapter<E> {

    private final AncestorsValidator validator;

    public AncestorsValidationAdapter(AncestorsValidator validator) {
        this.validator = validator;
    }


    @Override
    public ValidationTrigger<E> trigger() {
        return entityFields -> true;
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
    public ValidationError validate(EntityChange<E> entityChange, CurrentEntityState currentState) {
        return validator.validate(currentState);
    }
}
