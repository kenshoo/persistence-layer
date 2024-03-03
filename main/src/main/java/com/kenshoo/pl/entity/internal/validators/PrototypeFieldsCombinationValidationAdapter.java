package com.kenshoo.pl.entity.internal.validators;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.spi.PrototypeFieldsCombinationValidator;

import java.util.Map;
import java.util.stream.Stream;

public class PrototypeFieldsCombinationValidationAdapter<E extends EntityType<E>> implements ChangeValidatorAdapter<E> {

    private final PrototypeFieldsCombinationValidator prototypeFieldsCombinationValidator;
    private final Map<EntityFieldPrototype<?>, EntityField<E, ?>> fieldsMapping;
    private final ValidationTrigger<E> trigger;

    public PrototypeFieldsCombinationValidationAdapter(PrototypeFieldsCombinationValidator prototypeFieldsCombinationValidator, Map<EntityFieldPrototype<?>, EntityField<E, ?>> fieldsMapping) {
        this.prototypeFieldsCombinationValidator = prototypeFieldsCombinationValidator;
        this.fieldsMapping = fieldsMapping;
        this.trigger = new AnyFieldsTrigger<>(fieldsMapping.values().stream());
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
        return Stream.concat(fieldsMapping.values().stream(), prototypeFieldsCombinationValidator.ancestorsFields());
    }

    @Override
    public ValidationError validate(EntityChange<E> entityChange, CurrentEntityState currentState,  FinalEntityState finalState) {
        if(prototypeFieldsCombinationValidator.validateWhen().test(currentState)) {
            FieldsCombination<E> fieldsCombination = new FieldsCombination<>(entityChange, currentState, fieldsMapping.values().stream(), entityChange.getChangeOperation());
            PrototypeFieldsCombination<E> prototypeFieldsCombination = new PrototypeFieldsCombination<>(fieldsMapping, fieldsCombination);
            return prototypeFieldsCombinationValidator.validate(prototypeFieldsCombination);
        } else {
            return null;
        }
    }
}
