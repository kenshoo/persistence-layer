package com.kenshoo.pl.entity;

/**
 * Represents a <i>transient</i> value stored in the entity, meaning it is never persisted to the DB.<br>
 * Transient objects can be set in commands, and the basic PL flow will ignore them and simply carry them through the flow.<br>
 * They are useful for cases when a client needs to pass additional information about an entity and consume it using some custom logic, e.g. a custom {@link com.kenshoo.pl.entity.spi.OutputGenerator}.
 *
 * @param <E> the entity type
 * @param <T> the value type
 */
@SuppressWarnings("unused")
public interface TransientEntityObject<E extends EntityType<E>, T> {

    /**
     * @return the entity type to which this object belongs
     */
    EntityType<E> getEntityType();

    /**
     * @return the name of this object, which will also be its string representation
     */
    String getName();
}
