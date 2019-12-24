package com.kenshoo.pl.entity.spi;

import com.kenshoo.pl.entity.*;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

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
     * @param changeOperation operation
     * @param changeContext the context of the operation
     */
    void enrich(Collection<? extends ChangeEntityCommand<E>> commands, ChangeOperation changeOperation, ChangeContext changeContext);

    /**
     * return stream of enriched fields according to input commands.
     *
     * @return the fields should be enriched
     */
    Stream<EntityField<E, ?>> fieldsToEnrich();

    /**
     * @param commands to enrich
     *
     * @return indicator that enricher should be run
     */
    boolean shouldRun(Collection<? extends ChangeEntityCommand<E>> commands);

}
