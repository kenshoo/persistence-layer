package com.kenshoo.pl.entity;

import java.util.Collection;

public interface CommandToValuesStrategy<E extends EntityType<E>> {

    Object[] getValues(Collection<EntityField<E, ?>> fields, EntityChange<E> cmd);

}
