package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.CurrentEntityState;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.FieldsValueMap;
import com.kenshoo.pl.entity.spi.FieldsCombinationValidator;

import java.util.Map;

public class OverrideFieldsCombination<E extends EntityType<E>> implements FieldsValueMap<E> {

    private final FieldsValueMap<E> fieldsValueMap;
    private final Map<EntityField<E,?>, FieldsCombinationValidator.Substitution<E, ?>> overrideFieldValueFunctions;
    private final CurrentEntityState currentState;

    public OverrideFieldsCombination(CurrentEntityState currentState, FieldsValueMap<E> fieldsValueMap, Map<EntityField<E, ?>, FieldsCombinationValidator.Substitution<E, ?>> overrideFieldValueFunctions) {
        this.currentState = currentState;
        this.fieldsValueMap = fieldsValueMap;
        this.overrideFieldValueFunctions = overrideFieldValueFunctions;

    }

    @Override
    public <T> boolean containsField(EntityField<E, T> field) {
        return fieldsValueMap.containsField(field);
    }

    @Override
    public <T> T get(EntityField<E, T> field) {
        T value = fieldsValueMap.get(field);
        return overrideValue(field, value);
    }


    private <T> T overrideValue(EntityField<E, T> field, T result) {
        if(overrideFieldValueFunctions.isEmpty()) {
            return result;
        }

        if(overrideFieldValueFunctions.containsKey(field)) {
            //noinspection unchecked
            FieldsCombinationValidator.Substitution<E, T> overrideFieldFunction = (FieldsCombinationValidator.Substitution<E, T>) overrideFieldValueFunctions.get(field);
            if(overrideFieldFunction.overrideWhen().test(result)) {
                return overrideFieldFunction.overrideHow().apply(currentState);
            }
        }
        return result;
    }
}
