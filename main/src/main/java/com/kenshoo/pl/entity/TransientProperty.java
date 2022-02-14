package com.kenshoo.pl.entity;

/**
 * Represents a <i>transient</i> property stored in a command, meaning it is never persisted to the DB.<br>
 * The basic PL flow will ignore these properties and simply carry them through the flow.<br>
 * It can be useful for cases where a client wants to pass additional information to be consumed at the end of the flow, without saving it to the DB.<br>
 * The first use-case to be implemented within PL is a property holding a custom description for auditing.
 */
public interface TransientProperty<T> {

    /**
     * @return the name of the property
     */
    String getName();

    /**
     * @return the type of value the property can have
     */
    Class<T> getType();
}
