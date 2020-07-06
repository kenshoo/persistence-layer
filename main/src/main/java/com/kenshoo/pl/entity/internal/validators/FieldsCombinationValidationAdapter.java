package com.kenshoo.pl.entity.internal.validators;

import com.kenshoo.pl.entity.Entity;
import com.kenshoo.pl.entity.EntityChange;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.SupportedChangeOperation;
import com.kenshoo.pl.entity.ValidationError;
import com.kenshoo.pl.entity.internal.OverrideFieldsCombination;
import com.kenshoo.pl.entity.internal.ResultingFieldsCombination;
import com.kenshoo.pl.entity.spi.FieldsCombinationValidator;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FieldsCombinationValidationAdapter<E extends EntityType<E>> implements EntityChangeValidator<E> {

    private final FieldsCombinationValidator<E> validator;

    public FieldsCombinationValidationAdapter(FieldsCombinationValidator<E> validator) {
        this.validator = validator;
    }

    @Override
    public Stream<EntityField<E, ?>> validatedFields() {
        return validator.validatedFields();
    }

    @Override
    public SupportedChangeOperation getSupportedChangeOperation() {
        return SupportedChangeOperation.CREATE_AND_UPDATE;
    }

    @Override
    public Stream<? extends EntityField<?, ?>> fetchFields() {
        return Stream.concat(validator.validatedFields(), validator.fetchFields());
    }

    @Override
    public ValidationError validate(EntityChange<E> entityChange, Entity currentState) {
        if(validator.validateWhen().test(currentState)) {
            ResultingFieldsCombination<E> resultingFieldsCombination = new ResultingFieldsCombination<>(entityChange, currentState, validator.validatedFields(), entityChange.getChangeOperation());
            if (hasSubstitutions()) {
                return validator.validate(new OverrideFieldsCombination<>(currentState, resultingFieldsCombination, mapFieldToOverrideFunction()));
            } else {
                return validator.validate(resultingFieldsCombination);
            }
        } else {
            return null;
        }
    }

    private boolean hasSubstitutions() {
        return validator.substitutions().findAny().isPresent();
    }

    private Map<EntityField<E, ?>, FieldsCombinationValidator.Substitution<E, ?>> mapFieldToOverrideFunction() {
        return validator.substitutions().
                collect(Collectors.toMap(FieldsCombinationValidator.Substitution::overrideField, Function.identity()));
    }
}
