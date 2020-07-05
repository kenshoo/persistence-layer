package com.kenshoo.pl.entity.internal.validators;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;

import java.util.Objects;

public class FieldTrigger<E extends EntityType<E>> implements ValidationTrigger<E> {

    private final EntityField<E, ?> triggerField;

    public FieldTrigger(EntityField<E, ?> triggerField) {
        this.triggerField = triggerField;
    }

    @Override
    public boolean shouldValidate(EntityField<E, ?> entityField) {
        return triggerField.equals(entityField);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FieldTrigger<?> that = (FieldTrigger<?>) o;
        return Objects.equals(triggerField, that.triggerField);
    }

    @Override
    public int hashCode() {
        return Objects.hash(triggerField);
    }
}
