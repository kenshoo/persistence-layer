package com.kenshoo.pl.entity;

import java.util.Collection;

public class EntityDeleteResult<E extends EntityType<E>, ID extends Identifier<E>> extends EntityChangeResult<E, ID, DeleteEntityCommand<E, ID>> {
    public EntityDeleteResult(DeleteEntityCommand<E, ID> command) {
        super(command);
    }

    public EntityDeleteResult(DeleteEntityCommand<E, ID> command, Collection<ValidationError> errors) {
        super(command, errors);
    }

    @Override
    public ID getIdentifier() {
        return getCommand().getIdentifier();
    }
}
