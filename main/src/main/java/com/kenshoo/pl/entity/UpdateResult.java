package com.kenshoo.pl.entity;

public class UpdateResult<E extends EntityType<E>, ID extends Identifier<E>> extends ChangeResult<E, ID, UpdateEntityCommand<E, ID>> {
    public UpdateResult(Iterable<EntityUpdateResult<E, ID>> changeResults, PersistentLayerStats stats) {
        super(changeResults, stats);
    }

    // For use by tests that don't care about statistics
    public UpdateResult(Iterable<EntityUpdateResult<E, ID>> changeResults) {
        this(changeResults, new PersistentLayerStats());
    }
}
