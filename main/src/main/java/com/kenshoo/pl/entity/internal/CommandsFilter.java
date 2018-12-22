package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.ChangeContext;
import com.kenshoo.pl.entity.ChangeEntityCommand;
import com.kenshoo.pl.entity.EntityType;

import java.util.Collection;

public interface CommandsFilter<E extends EntityType<E>> {

    <C extends ChangeEntityCommand<E>> Collection<C> filter(Collection<C> commands, final ChangeContext changeContext);

}
