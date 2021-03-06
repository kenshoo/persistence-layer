package com.kenshoo.pl.simulation;

import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.Identifier;

public class ActualMutatorError<E extends EntityType<E>> {
    private final Identifier<E> id;
    private final String description;

    public Identifier<E> getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public ActualMutatorError(Identifier<E> id, String description) {
        this.id = id;
        this.description = description;
    }
}