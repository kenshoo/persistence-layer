package com.kenshoo.pl.entity;

import java.util.List;

public class FinalEntityState implements Entity {

    private final CurrentEntityState currentState;
    private final EntityChange<? extends EntityType<?>> change;

    public FinalEntityState(CurrentEntityState currentState, EntityChange<? extends EntityType<?>> change) {
        this.currentState = currentState;
        this.change = change;
    }

    @Override
    public boolean containsField(EntityField<?, ?> field) {
        return change.containsField((EntityField)field) || currentState.containsField(field);
    }

    @Override
    public <T> T get(EntityField<?, T> field) {
        return field.getEntityType() == change.getEntityType() && change.containsField((EntityField)field)
                ? (T)change.get((EntityField)field)
                : currentState.get(field);
    }

    @Override
    public <CHILD extends EntityType<CHILD>> List<FieldsValueMap<CHILD>> getMany(CHILD type) {
        throw new UnsupportedOperationException("not yet implemented");
    }

}
