package com.kenshoo.pl.entity;

public class CreateResult<E extends EntityType<E>, ID extends Identifier<E>> extends ChangeResult<E, ID, CreateEntityCommand<E>> {

    public CreateResult(Iterable<EntityCreateResult<E, ID>> changeResults, PersistentLayerStats stats) {
        super(changeResults, stats);
    }

    // For use by tests that don't care about statistics
    public CreateResult(Iterable<EntityCreateResult<E, ID>> changeResults) {
        this(changeResults, new PersistentLayerStats());
    }
}
