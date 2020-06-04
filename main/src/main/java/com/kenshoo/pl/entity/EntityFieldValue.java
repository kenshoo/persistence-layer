package com.kenshoo.pl.entity;

public class EntityFieldValue {
    private final EntityField<?, ?> entityField;
    private final Object value;

    public EntityFieldValue(final EntityField<?, ?> entityField, final Object value) {
        this.entityField = entityField;
        this.value = value;
    }

    public EntityField<?, ?> getField() {
        return entityField;
    }

    public Object getValue() {
        return value;
    }


    public String getFieldName() {
        return entityField.getEntityType().getName() + "." + entityField;
    }

    @Override
    public String toString() {
        return getFieldName() + "=" + value;
    }
}
