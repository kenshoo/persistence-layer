package com.kenshoo.pl.simulation;

import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.Identifier;

public class ComparisonMismatch<E extends EntityType<E>> {
    private final Identifier<E> id;
    private final String description;

    public Identifier<E> getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public ComparisonMismatch(Identifier<E> id, String description) {
        this.id = id;
        this.description = description;
    }

    @Override
    public String toString() {
        return "{Id=" + id + ", Error: " + description + "}";
    }
}