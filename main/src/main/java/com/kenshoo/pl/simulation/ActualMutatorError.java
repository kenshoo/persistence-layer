package com.kenshoo.pl.simulation;

import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.Identifier;

public class ActualMutatorError<E extends EntityType<E>, ID extends Identifier<E>> {
    private final ID id;
    private final String description;

    public ID getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public ActualMutatorError(ID id, String description) {
        this.id = id;
        this.description = description;
    }
}