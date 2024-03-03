package com.kenshoo.pl.entity.internal.validators;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.spi.FieldsCombinationValidator;
import java.util.stream.Stream;

public class FieldsCombinationValidationAdapter<E extends EntityType<E>> implements ChangeValidatorAdapter<E> {

    private final FieldsCombinationValidator<E> validator;
    private final ValidationTrigger<E> trigger;

    public FieldsCombinationValidationAdapter(FieldsCombinationValidator<E> validator) {
        this.validator = validator;
        this.trigger = new AnyFieldsTrigger<>(validator.validatedFields());
    }

    @Override
    public ValidationTrigger<E> trigger() {
        return trigger;
    }

    @Override
    public SupportedChangeOperation getSupportedChangeOperation() {
        return SupportedChangeOperation.CREATE_AND_UPDATE;
    }

    @Override
    public Stream<? extends EntityField<?, ?>> fieldsToFetch() {
        return Stream.concat(validator.validatedFields(), validator.fetchFields());
    }

    @Override
    public ValidationError validate(EntityChange<E> entityChange, CurrentEntityState currentState, FinalEntityState finalState) {
        if(validator.validateWhen().test(finalState)) {
            FieldsCombination<E> fieldsCombination = new FieldsCombination<>(entityChange, currentState, validator.validatedFields(), entityChange.getChangeOperation());
                return validator.validate(fieldsCombination);
        } else {
            return null;
        }
    }
}
