package com.kenshoo.pl.entity.internal.validators;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

public class FieldTrigger<E extends EntityType<E>> implements ValidationTrigger<E> {

    private final EntityField<E, ?> triggerField;

    public FieldTrigger(EntityField<E, ?> triggerField) {
        this.triggerField = triggerField;
    }

    @Override
    public boolean triggeredByFields(Collection<? extends EntityField<E, ?>> entityFields) {
        return entityFields.stream().anyMatch(triggerField::equals);
    }
}
