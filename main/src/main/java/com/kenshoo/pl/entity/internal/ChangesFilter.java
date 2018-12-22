package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.ChangeContext;
import com.kenshoo.pl.entity.ChangeOperation;
import com.kenshoo.pl.entity.EntityChange;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.spi.ChangeOperationSpecificConsumer;

import java.util.Collection;

public interface ChangesFilter<E extends EntityType<E>> extends ChangeOperationSpecificConsumer<E> {

    <T extends EntityChange<E>> Collection<T> filter(Collection<T> changes, final ChangeOperation changeOperation, final ChangeContext changeContext);

}
