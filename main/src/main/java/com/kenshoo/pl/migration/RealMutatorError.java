package com.kenshoo.pl.migration;

import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.Identifier;

public class RealMutatorError<E extends EntityType<E>, ID extends Identifier<E>> {
    private final ID id;
    private final String description;

    public ID getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public RealMutatorError(ID id, String description) {
        this.id = id;
        this.description = description;
    }
}