package com.kenshoo.pl.entity;

import java.util.Collection;

public class DeleteResult<E extends EntityType<E>, ID extends Identifier<E>> extends ChangeResult<E, ID, DeleteEntityCommand<E, ID>> {

    public DeleteResult(Collection<EntityDeleteResult<E, ID>> changeResults, PersistentLayerStats stats) {
        super(changeResults, stats);
    }

    // For use by tests that don't care about statistics
    public DeleteResult(Collection<EntityDeleteResult<E, ID>> changeResults) {
        this(changeResults, new PersistentLayerStats());
    }
}
