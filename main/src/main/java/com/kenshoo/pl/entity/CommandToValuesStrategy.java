package com.kenshoo.pl.entity;

import java.util.Collection;
import java.util.Optional;

public interface CommandToValuesStrategy<E extends EntityType<E>> {

    Optional<Object[]> getValues(Collection<EntityField<E, ?>> fields, EntityChange<E> cmd, ChangeContext ctx);

}
