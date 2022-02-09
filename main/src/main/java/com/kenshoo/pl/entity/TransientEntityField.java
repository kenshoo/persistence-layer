package com.kenshoo.pl.entity;

/**
 * Represents an entity field which is <i>transient</i>, meaning it is never persisted to the DB.<br>
 * Transient fields can be set in commands, and the basic PL flow will ignore them and simply carry them through the flow.<br>
 * They are useful for cases when a client needs to pass additional information about an entity and consume it using some custom logic, e.g. a custom {@link com.kenshoo.pl.entity.spi.OutputGenerator}.
 *
 * @param <E> the entity type
 * @param <T> the value type
 */
@SuppressWarnings("unused")
public interface TransientEntityField<E extends EntityType<E>, T> {

    /**
     * @return the entity type to which this field belongs
     */
    EntityType<E> getEntityType();

    /**
     * @return the name of this field, which will also be its string representation
     */
    String getName();
}
