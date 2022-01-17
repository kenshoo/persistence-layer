package com.kenshoo.pl.entity.spi;

import com.kenshoo.pl.entity.ChangeContext;
import com.kenshoo.pl.entity.ChangeEntityCommand;
import com.kenshoo.pl.entity.ChangeOperation;
import com.kenshoo.pl.entity.EntityType;

import java.util.Collection;

/**
 * The finalizers are called after the current state is fetched and
 * the changes are validated recursively.
 *
 * @param <E>
 */
public interface PostValidateCommandFinalizer<E extends EntityType<E>> extends CurrentStateConsumer<E> {

    /**
     * "Finalizes" the commands with system-imposed changes.
     *  @param commands      commands to finalize
     * @param changeOperation operation
     * @param changeContext the context of the operation
     */
    void finalize(Collection<? extends ChangeEntityCommand<E>> commands, ChangeOperation changeOperation, ChangeContext changeContext);
}
