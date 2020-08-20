package com.kenshoo.pl.entity.internal.validators;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;

import java.util.Collection;


public interface ValidationTrigger<E extends EntityType<E>> {

    boolean triggeredByFields(Collection<? extends EntityField<E, ?>> entityFields);
}
