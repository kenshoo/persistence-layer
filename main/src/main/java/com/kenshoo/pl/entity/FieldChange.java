package com.kenshoo.pl.entity;

public class FieldChange<E extends EntityType<E>, T> {

    private final EntityField<E, T> field;
    private final T value;

    public FieldChange(EntityField<E, T> field, T value) {
        this.field = field;
        this.value = value;
    }

    public EntityField<E, T> getField() {
        return field;
    }

    public T getValue() {
        return value;
    }
}
