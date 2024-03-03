package com.kenshoo.pl.entity;

import java.util.Collection;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

/**
 * The final state of a certain combination of fields in the database.
 * This will be the state of fields after all changes have been applied on the current state and contains the final values of the entity's fields.
 *
 */
public class FieldsCombination<E extends EntityType<E>> implements FieldsValueMap<E> {
    private final EntityChange<E> entityChange;
    private final CurrentEntityState currentState;
    private final Collection<EntityField<E, ?>> fields;
    private final ChangeOperation changeOperation;

    public FieldsCombination(EntityChange<E> entityChange, CurrentEntityState currentState, Stream<EntityField<E, ?>> fields, ChangeOperation changeOperation) {
        this.entityChange = entityChange;
        this.currentState = currentState;
        this.fields = fields.collect(toSet());
        this.changeOperation = changeOperation;
    }

    @Override
    public <T> boolean containsField(EntityField<E, T> field) {
        return entityChange.containsField(field) ||  currentState.containsField(field);
    }

    @Override
    public <T> T get(EntityField<E, T> field) {
        if (!fields.contains(field)) {
            throw new IllegalArgumentException("Illegal field: " + field);
        }

        if (entityChange.isFieldChanged(field)) {
            return entityChange.get(field);
        } else {
            if (changeOperation == ChangeOperation.UPDATE) {
                return  currentState.get(field);
            } else {
                return null;
            }
        }
    }
}
