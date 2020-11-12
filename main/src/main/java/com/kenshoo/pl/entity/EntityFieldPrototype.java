package com.kenshoo.pl.entity;

/**
 * Field "prototypes" are "meta" information on fields - a markers that mark certain behaviors shared between entities.
 * For example, tracking template field should have the same validations no matter whether it belongs to
 * a keyword or an ad. Referring to these fields by "prototypes" allows implementing these behaviors
 * in a generic way.
 *
 * @param <T> type of the field
 */
public class EntityFieldPrototype<T> {

    private final String name;

    public EntityFieldPrototype(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
