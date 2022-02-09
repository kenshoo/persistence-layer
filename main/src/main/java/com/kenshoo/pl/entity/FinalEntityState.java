package com.kenshoo.pl.entity;

import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

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

    /**
     * Fetch the value of the given transient property from this final state, if exists.<br>
     * The value is fetched from the underlying command only (no transient properties exist in an {@code Entity})
     *
     * @param transientProperty the property whose value is to be fetched; required
     * @param <T> the type of value to be fetched
     * @return the value, or {@code Optional.empty()} if it does not exist
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> Optional<T> get(final TransientEntityProperty<?, T> transientProperty) {
        requireNonNull(transientProperty, "A transient property is required");
        return (Optional<T>)(change.get((TransientEntityProperty)transientProperty));
    }

    @Override
    public <CHILD extends EntityType<CHILD>> List<FieldsValueMap<CHILD>> getMany(CHILD type) {
        throw new UnsupportedOperationException("Final state of children is unknown at this point");
    }

}
