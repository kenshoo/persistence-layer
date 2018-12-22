package com.kenshoo.pl.entity;

import java.util.Collection;

public class CreateResult<E extends EntityType<E>, ID extends Identifier<E>> extends ChangeResult<E, ID, CreateEntityCommand<E>> {

    public CreateResult(Collection<EntityCreateResult<E, ID>> changeResults, PersistentLayerStats stats) {
        super(changeResults, stats);
    }

    // For use by tests that don't care about statistics
    public CreateResult(Collection<EntityCreateResult<E, ID>> changeResults) {
        this(changeResults, new PersistentLayerStats());
    }
}
