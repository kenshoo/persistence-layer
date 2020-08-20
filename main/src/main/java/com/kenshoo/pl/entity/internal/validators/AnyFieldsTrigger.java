package com.kenshoo.pl.entity.internal.validators;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AnyFieldsTrigger <E extends EntityType<E>> implements ValidationTrigger<E>{

    private final Set<EntityField<E, ?>> triggerFields;

    public AnyFieldsTrigger(Stream<EntityField<E, ?>> triggerFields) {
        this.triggerFields = triggerFields.collect(Collectors.toSet());
    }

    @Override
    public boolean triggeredByFields(Collection<? extends EntityField<E, ?>> entityFields) {
        return entityFields.stream().anyMatch(field -> triggerFields.contains(field));
    }
}
