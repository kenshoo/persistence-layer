package com.kenshoo.pl.entity.spi.helpers;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.kenshoo.pl.entity.ChangeContext;
import com.kenshoo.pl.entity.ChangeOperation;
import com.kenshoo.pl.entity.CurrentEntityState;
import com.kenshoo.pl.entity.EntityChange;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityFieldPrototype;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.internal.EntityTypeReflectionUtil;
import com.kenshoo.pl.entity.internal.validators.EntityChangeValidator;
import com.kenshoo.pl.entity.internal.validators.FieldComplexValidationAdapter;
import com.kenshoo.pl.entity.internal.validators.FieldValidationAdapter;
import com.kenshoo.pl.entity.internal.validators.FieldsCombinationValidationAdapter;
import com.kenshoo.pl.entity.internal.validators.ImmutableFieldValidationAdapter;
import com.kenshoo.pl.entity.internal.validators.PrototypeFieldComplexValidationAdapter;
import com.kenshoo.pl.entity.internal.validators.PrototypeFieldValidationAdapter;
import com.kenshoo.pl.entity.internal.validators.PrototypeFieldsCombinationValidationAdapter;
import com.kenshoo.pl.entity.internal.validators.RequiredFieldValidationAdapter;
import com.kenshoo.pl.entity.spi.ChangeValidator;
import com.kenshoo.pl.entity.spi.ChangesValidator;
import com.kenshoo.pl.entity.spi.FieldComplexValidator;
import com.kenshoo.pl.entity.spi.FieldValidator;
import com.kenshoo.pl.entity.spi.FieldsCombinationValidator;
import com.kenshoo.pl.entity.spi.ImmutableFieldValidator;
import com.kenshoo.pl.entity.spi.PrototypeFieldComplexValidator;
import com.kenshoo.pl.entity.spi.PrototypeFieldValidator;
import com.kenshoo.pl.entity.spi.PrototypeFieldsCombinationValidator;
import com.kenshoo.pl.entity.spi.RequiredFieldValidator;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

public class EntityChangeCompositeValidator<E extends EntityType<E>> implements ChangesValidator<E> {

    private final Multimap<EntityField<E, ?>, EntityChangeValidator<E>> changesValidators = HashMultimap.create();

    public void register(E entityType, ChangeValidator validator) {
        if (validator instanceof FieldComplexValidator) {
            //noinspection unchecked
            register((FieldComplexValidator) validator);
        } else if (validator instanceof FieldValidator) {
            //noinspection unchecked
            register((FieldValidator) validator);
        } else if (validator instanceof FieldsCombinationValidator) {
            //noinspection unchecked
            register((FieldsCombinationValidator) validator);
        } else if (validator instanceof ImmutableFieldValidator) {
            //noinspection unchecked
            register((ImmutableFieldValidator) validator);
        } else if (validator instanceof PrototypeFieldValidator) {
            //noinspection unchecked
            register(entityType, (PrototypeFieldValidator) validator);
        } else if ((validator instanceof PrototypeFieldsCombinationValidator)) {
            register(entityType, (PrototypeFieldsCombinationValidator) validator);
        } else if (validator instanceof PrototypeFieldComplexValidator) {
            //noinspection unchecked
            register(entityType, (PrototypeFieldComplexValidator) validator);
        } else if (validator instanceof RequiredFieldValidator) {
            //noinspection unchecked
            register((RequiredFieldValidator) validator);
        }
    }

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

    public void register(EntityChangeValidator<E> validator) {
        validator.validatedFields().forEach(field -> changesValidators.put(field, validator));
    }

    public <T> void register(E entityType, PrototypeFieldValidator<T> validator) {
        Set<EntityField<E, T>> entityFields = EntityTypeReflectionUtil.getFieldsByPrototype(entityType, validator.getPrototype());
        if (entityFields.isEmpty()) {
            throw new IllegalArgumentException("Can not find entity field by prototype: " + validator.getPrototype());
        }
        for (EntityField<E, T> entityField : entityFields) {
            changesValidators.put(entityField, new PrototypeFieldValidationAdapter<>(entityField, validator));
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
            changesValidators.put(entityField, new PrototypeFieldComplexValidationAdapter<>(entityField, validator));
        }
    }

    @Override
    public void validate(Collection<? extends EntityChange<E>> entityChanges, ChangeOperation changeOperation, ChangeContext changeContext) {
        entityChanges.forEach(entityChange -> {
            CurrentEntityState currentState = changeContext.getEntity(entityChange);
            Collection<EntityChangeValidator<E>> validators = findValidators(entityChange);
            validators.stream()
                    .filter(validator -> validator.getSupportedChangeOperation().supports(changeOperation))
                    .map(validator -> validator.validate(entityChange, currentState))
                    .filter(Objects::nonNull)
                    .forEach(validationError -> changeContext.addValidationError(entityChange, validationError));
        });
    }

    @Override
    public Stream<EntityField<?, ?>> requiredFields(Collection<? extends EntityField<E, ?>> fieldsToUpdate, ChangeOperation changeOperation) {
        return fieldsToUpdate.stream()
                .flatMap(field -> changesValidators.get(field).stream())
                .filter(validator -> validator.getSupportedChangeOperation().supports(changeOperation))
                .flatMap(validator -> validator.fetchFields());
    }

    private Collection<EntityChangeValidator<E>> findValidators(EntityChange<E> entityChange) {
        return entityChange.getChangedFields()
                .flatMap(field -> changesValidators.get(field).stream())
                .collect(toSet());
    }
}
