package com.kenshoo.pl.entity.spi.helpers;

import com.kenshoo.pl.entity.ChangeContext;
import com.kenshoo.pl.entity.ChangeOperation;
import com.kenshoo.pl.entity.CurrentEntityState;
import com.kenshoo.pl.entity.EntityChange;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityFieldPrototype;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.internal.EntityTypeReflectionUtil;
import com.kenshoo.pl.entity.internal.validators.*;
import com.kenshoo.pl.entity.spi.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EntityChangeCompositeValidator<E extends EntityType<E>> implements ChangesValidator<E> {

    private final List<ChangeValidatorAdapter<E>> triggeredChangeValidators = new ArrayList<>();

    public void register(FieldsCombinationValidator<E> validator) {
        register(new FieldsCombinationValidationAdapter<>(validator));
    }

    public <T> void register(FieldValidator<E, T> validator) {
        register(new FieldValidationAdapter<>(validator));
    }

    public <T> void register(FieldComplexValidator<E, T> validator) {
        register(new FieldComplexValidationAdapter<>(validator));
    }

    public <T> void register(ImmutableFieldValidator<E, T> validator) {
        register(new ImmutableFieldValidationAdapter<>(validator));
    }

    public <T> void register(RequiredFieldValidator<E, T> validator) {
        register(new RequiredFieldValidationAdapter<>(validator));
    }

    public void register(AncestorsValidator validator) {
        register(new AncestorsValidationAdapter<>(validator));
    }

    public void register(EntityChangeValidator<E> validator) {
        register(new EntityChangeValidatorAdapter<>(validator));
    }

    public <T> void register(E entityType, PrototypeFieldValidator<T> validator) {
        Set<EntityField<E, T>> entityFields = EntityTypeReflectionUtil.getFieldsByPrototype(entityType, validator.getPrototype());
        if (entityFields.isEmpty()) {
            throw new IllegalArgumentException("Can not find entity field by prototype: " + validator.getPrototype());
        }
        for (EntityField<E, T> entityField : entityFields) {
            register(new PrototypeFieldValidationAdapter<>(entityField, validator));
        }
    }

    public void register(E entityType, PrototypeFieldsCombinationValidator validator) {
        Map<EntityFieldPrototype<?>, EntityField<E, ?>> fieldMapping = EntityTypeReflectionUtil.getFieldMappingByPrototype(entityType, validator.getPrototypes());
        if (fieldMapping.size() == validator.getPrototypes().size()) {
            register(new PrototypeFieldsCombinationValidationAdapter<>(validator, fieldMapping));
        } else {
            for (EntityFieldPrototype<?> entityFieldPrototype : validator.getPrototypes()) {
                if (!fieldMapping.containsKey(entityFieldPrototype)) {
                    throw new IllegalArgumentException("Can not find entity field by prototype: " + entityFieldPrototype);
                }
            }
        }
    }

    public <T> void register(E entityType, PrototypeFieldComplexValidator<T> validator) {
        Set<EntityField<E, T>> entityFields = EntityTypeReflectionUtil.getFieldsByPrototype(entityType, validator.getPrototype());
        if (entityFields.isEmpty()) {
            throw new IllegalArgumentException("Can not find entity field by prototype: " + validator.getPrototype());
        }
        for (EntityField<E, T> entityField : entityFields) {
            register(new PrototypeFieldComplexValidationAdapter<>(entityField, validator));
        }
    }

    @Override
    public void validate(Collection<? extends EntityChange<E>> entityChanges, ChangeOperation changeOperation, ChangeContext changeContext) {
        entityChanges.forEach(entityChange -> {
            CurrentEntityState currentState = changeContext.getEntity(entityChange);
            Collection<? extends EntityField<E, ?>> fieldsToUpdate = entityChange.getChangedFields().collect(Collectors.toList());
            findValidatorsTriggeredByFields(fieldsToUpdate, changeOperation)
                    .map(validator -> validator.validate(entityChange, currentState))
                    .filter(Objects::nonNull)
                    .forEach(validationError -> changeContext.addValidationError(entityChange, validationError));
        });
    }

    @Override
    public Stream<EntityField<?, ?>> requiredFields(Collection<? extends EntityField<E, ?>> fieldsToUpdate, ChangeOperation changeOperation) {
        return findValidatorsTriggeredByFields(fieldsToUpdate, changeOperation)
                .flatMap(ChangeValidatorAdapter::fieldsToFetch);
    }

    public void register(ChangeValidatorAdapter<E> validatorAdapter) {
        triggeredChangeValidators.add(validatorAdapter);
    }

    private Stream<ChangeValidatorAdapter<E>> findValidatorsTriggeredByFields(Collection<? extends EntityField<E, ?>> entityFields, ChangeOperation changeOperation) {
        return triggeredChangeValidators.stream().
                filter(triggeredValidator -> triggeredValidator.trigger().triggeredByFields(entityFields)).
                filter(triggeredValidator -> triggeredValidator.getSupportedChangeOperation().supports(changeOperation));
    }
}
