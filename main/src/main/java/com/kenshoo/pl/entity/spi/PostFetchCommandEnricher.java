package com.kenshoo.pl.entity.spi;

import com.kenshoo.pl.entity.ChangeContext;
import com.kenshoo.pl.entity.ChangeEntityCommand;
import com.kenshoo.pl.entity.ChangeOperation;
import com.kenshoo.pl.entity.EntityType;

import java.util.Collection;

/**
 * As opposed to {@link FieldValueSupplier} which is implemented by the end-user flow, this interface is implemented
 * by operations that are considered inherent part of the persistence layer. For example, if every newly generated
 * entity should get a unique affcode, this logic is implemented with an enricher (by implementing this interface).
 * The enrichers are called after the current state is fetched and user-specified suppliers are resolved but before
 * the changes are validated.
 *
 * @param <E>
 */
public interface PostFetchCommandEnricher<E extends EntityType<E>> extends ChangeOperationSpecificConsumer<E> {

    /**
     * "Enriches" the commands with system-imposed changes.
     *  @param commands      commands to enrich
     * @param changeOperation
     * @param changeContext the context of the operation
     */
    void enrich(Collection<? extends ChangeEntityCommand<E>> commands, ChangeOperation changeOperation, ChangeContext changeContext);

}
