package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.spi.CurrentStateConsumer;

import java.util.Collection;

public interface ChangesFilter<E extends EntityType<E>> extends CurrentStateConsumer<E> {

    <T extends ChangeEntityCommand<E>> Collection<T> filter(Collection<T> changes, final ChangeOperation changeOperation, final ChangeContext changeContext);

}
