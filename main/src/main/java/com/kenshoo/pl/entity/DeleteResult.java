package com.kenshoo.pl.entity;


public class DeleteResult<E extends EntityType<E>, ID extends Identifier<E>> extends ChangeResult<E, ID, DeleteEntityCommand<E, ID>> {

    public DeleteResult(Iterable<EntityDeleteResult<E, ID>> changeResults, PersistentLayerStats stats) {
        super(changeResults, stats);
    }

    // For use by tests that don't care about statistics
    public DeleteResult(Iterable<EntityDeleteResult<E, ID>> changeResults) {
        this(changeResults, new PersistentLayerStats());
    }
}
