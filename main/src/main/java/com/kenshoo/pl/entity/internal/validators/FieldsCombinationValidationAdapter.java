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
        Stream<EntityField<?, ?>> substitutionsFields = validator.substitutions().flatMap(FieldsCombinationValidator.Substitution::fetchFields);
        if (changeOperation == ChangeOperation.UPDATE) {
            return Stream.concat(validator.validatedFields(), substitutionsFields);
        } else {
            return substitutionsFields;
        }
    }

    @Override
    public ValidationError validate(EntityChange<E> entityChange, Entity entity, ChangeOperation changeOperation) {
        ResultingFieldsCombination<E> resultingFieldsCombination = new ResultingFieldsCombination<>(entityChange, entity, validator.validatedFields(), changeOperation);
        if (validator.substitutions().findAny() != null) {
            Map<EntityField<E, ?>, FieldsCombinationValidator.Substitution<E, ?>> overrideFunctions =
                            validator.substitutions().
                            collect(Collectors.toMap(FieldsCombinationValidator.Substitution::overrideField, Function.identity()));
            return validator.validate(new OverrideFieldsCombination<>(entity, resultingFieldsCombination, overrideFunctions));
        } else {
            return validator.validate(resultingFieldsCombination);
        }
    }
}
