package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.ChangeContext;
import com.kenshoo.pl.entity.ChangeOperation;
import com.kenshoo.pl.entity.EntityChange;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.spi.CurrentStateConsumer;

import java.util.Collection;

public interface ChangesFilter<E extends EntityType<E>> extends CurrentStateConsumer<E> {

    <T extends EntityChange<E>> Iterable<T> filter(Collection<T> changes, final ChangeOperation changeOperation, final ChangeContext changeContext);

}
