package com.kenshoo.pl.entity.internal.validators;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;

public interface ValidationTrigger<E extends EntityType<E>> {

    boolean triggeredByField(EntityField<E, ?> entityField);
}
