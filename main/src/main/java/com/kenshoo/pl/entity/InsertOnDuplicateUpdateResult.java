package com.kenshoo.pl.entity;

import java.util.Collection;

public class InsertOnDuplicateUpdateResult<E extends EntityType<E>, ID extends Identifier<E>> extends ChangeResult<E, ID, InsertOnDuplicateUpdateCommand<E, ID>> {

    public InsertOnDuplicateUpdateResult(Collection<EntityInsertOnDuplicateUpdateResult<E, ID>> changeResults, PersistentLayerStats stats) {
        super(changeResults, stats);
    }

    // For use by tests that don't care about statistics
    public InsertOnDuplicateUpdateResult(Collection<EntityInsertOnDuplicateUpdateResult<E, ID>> changeResults) {
        this(changeResults, new PersistentLayerStats());
    }

}
