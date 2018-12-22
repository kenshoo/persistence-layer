package com.kenshoo.pl.entity.spi;

import com.kenshoo.pl.entity.ChangeContext;
import com.kenshoo.pl.entity.ChangeOperation;
import com.kenshoo.pl.entity.EntityChange;
import com.kenshoo.pl.entity.EntityType;

import java.util.Collection;

/**
 * To be implemented by a component that produces some sort of output given the set of commands. Calling
 * output generators is the last stage of the persistence flow. All output generators are executed by the framework
 * in the same DB transaction.
 *
 * @param <E> entity type
 */
public interface OutputGenerator<E extends EntityType<E>> extends CurrentStateConsumer<E> {

    /**
     * Produces some sort of output given a set of entity changes.
     *  @param entityChanges a valid subset of the changes initially submitted to the persistence layer
     * @param changeOperation
     * @param changeContext modification context
     */
    void generate(Collection<? extends EntityChange<E>> entityChanges, ChangeOperation changeOperation, ChangeContext changeContext);

}
