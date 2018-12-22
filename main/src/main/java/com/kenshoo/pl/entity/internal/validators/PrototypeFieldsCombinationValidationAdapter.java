package com.kenshoo.pl.entity.internal.validators;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.ResultingFieldsCombination;
import com.kenshoo.pl.entity.spi.PrototypeFieldsCombinationValidator;

import java.util.Map;
import java.util.stream.Stream;

public class PrototypeFieldsCombinationValidationAdapter<E extends EntityType<E>> implements EntityChangeValidator<E> {

    private final PrototypeFieldsCombinationValidator prototypeFieldsCombinationValidator;
    private final Map<EntityFieldPrototype<?>, EntityField<E, ?>> fieldsMapping;

    public PrototypeFieldsCombinationValidationAdapter(PrototypeFieldsCombinationValidator prototypeFieldsCombinationValidator, Map<EntityFieldPrototype<?>, EntityField<E, ?>> fieldsMapping) {
        this.prototypeFieldsCombinationValidator = prototypeFieldsCombinationValidator;
        this.fieldsMapping = fieldsMapping;
    }

    @Override
    public Stream<EntityField<E, ?>> getValidatedFields() {
        return fieldsMapping.values().stream();
    }

    @Override
    public SupportedChangeOperation getSupportedChangeOperation() {
        return SupportedChangeOperation.CREATE_AND_UPDATE;
    }

    @Override
    public Stream<? extends EntityField<?, ?>> getFieldsToFetch(ChangeOperation changeOperation) {
        if (changeOperation == ChangeOperation.UPDATE) {
            return fieldsMapping.values().stream();
        } else {
            return Stream.empty();
        }
    }

    @Override
    public ValidationError validate(EntityChange<E> entityChange, Entity entity, ChangeOperation changeOperation) {
        FieldsValueMap<E> fieldsValueMap = new ResultingFieldsCombination<>(entityChange, entity, fieldsMapping.values().stream(), changeOperation);
        PrototypeFieldsCombination<E> prototypeFieldsCombination = new PrototypeFieldsCombination<>(fieldsMapping, fieldsValueMap);
        return prototypeFieldsCombinationValidator.validate(prototypeFieldsCombination);
    }
}
