package com.kenshoo.pl.entity.internal.validators;

import com.google.common.collect.ImmutableMap;
import com.kenshoo.pl.entity.CurrentEntityState;
import com.kenshoo.pl.entity.EntityChange;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.SupportedChangeOperation;
import com.kenshoo.pl.entity.ValidationError;
import com.kenshoo.pl.entity.spi.ImmutableFieldValidator;

import java.util.stream.Stream;

public class ImmutableFieldValidationAdapter<E extends EntityType<E>, T> implements ChangeValidatorAdapter<E> {

    private final ImmutableFieldValidator<E, T> validator;
    private final ValidationTrigger<E> trigger;

    public ImmutableFieldValidationAdapter(ImmutableFieldValidator<E, T> validator) {
        this.validator = validator;
        this.trigger = new FieldTrigger<>(validator.immutableField());
    }


    @Override
    public ValidationTrigger<E> trigger() {
        return trigger;
    }

    @Override
    public SupportedChangeOperation getSupportedChangeOperation() {
        return SupportedChangeOperation.UPDATE;
    }

    @Override
    public Stream<? extends EntityField<?, ?>> fieldsToFetch() {
        Stream<EntityField<?, ?>> fetchFields = validator.fetchFields();
        if(fetchFields != null) {
            return Stream.concat(Stream.of(validator.immutableField()), validator.fetchFields());
        } else {
           return Stream.of(validator.immutableField());
        }
    }

    @Override
    public ValidationError validate(EntityChange<E> entityChange, CurrentEntityState currentState) {
        if (entityChange.isFieldChanged(validator.immutableField()) && validator.immutableWhen().test(currentState)) {
            return new ValidationError(validator.getErrorCode(), validator.immutableField(), ImmutableMap.of("field", validator.immutableField().toString()));
        }
        return null;

    }
}
