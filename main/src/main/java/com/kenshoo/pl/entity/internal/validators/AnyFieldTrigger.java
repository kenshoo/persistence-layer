package com.kenshoo.pl.entity.internal.validators;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;

public class AnyFieldTrigger<E extends EntityType<E>> implements ValidationTrigger<E> {

    @Override
    public boolean triggeredByField(EntityField<E, ?> entityField) {
        return true;
    }
}
