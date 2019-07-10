package com.kenshoo.pl.entity.internal.validators;

import com.kenshoo.pl.entity.ChangeOperation;
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
    public Stream<EntityField<E, ?>> getValidatedFields() {
        return validator.validatedFields();
    }

    @Override
    public SupportedChangeOperation getSupportedChangeOperation() {
        return SupportedChangeOperation.CREATE_AND_UPDATE;
    }

    @Override
    public Stream<? extends EntityField<?, ?>> getFieldsToFetch(ChangeOperation changeOperation) {
        Stream<EntityField<?, ?>> substitutionsFields = validator.fetchFields();
        if (changeOperation == ChangeOperation.UPDATE) {
            return Stream.concat(validator.validatedFields(), substitutionsFields);
        } else {
            return substitutionsFields;
        }
    }

    @Override
    public ValidationError validate(EntityChange<E> entityChange, Entity entity, ChangeOperation changeOperation) {
        if(validator.validateWhen().test(entity)) {
            ResultingFieldsCombination<E> resultingFieldsCombination = new ResultingFieldsCombination<>(entityChange, entity, validator.validatedFields(), changeOperation);
            if (hasSubstitutions()) {
                return validator.validate(new OverrideFieldsCombination<>(entity, resultingFieldsCombination, mapFieldToOverrideFunction()));
            } else {
                return validator.validate(resultingFieldsCombination);
            }
        } else {
            return null;
        }
    }

    private boolean hasSubstitutions() {
        return validator.substitutions().findAny() != null;
    }

    private Map<EntityField<E, ?>, FieldsCombinationValidator.Substitution<E, ?>> mapFieldToOverrideFunction() {
        return validator.substitutions().
                collect(Collectors.toMap(FieldsCombinationValidator.Substitution::overrideField, Function.identity()));
    }
}
