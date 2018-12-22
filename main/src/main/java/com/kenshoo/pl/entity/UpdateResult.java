package com.kenshoo.pl.entity;

import java.util.Collection;

public class UpdateResult<E extends EntityType<E>, ID extends Identifier<E>> extends ChangeResult<E, ID, UpdateEntityCommand<E, ID>> {
    public UpdateResult(Collection<EntityUpdateResult<E, ID>> changeResults, PersistentLayerStats stats) {
        super(changeResults, stats);
    }

    // For use by tests that don't care about statistics
    public UpdateResult(Collection<EntityUpdateResult<E, ID>> changeResults) {
        this(changeResults, new PersistentLayerStats());
    }
}
