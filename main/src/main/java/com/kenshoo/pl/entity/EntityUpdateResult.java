package com.kenshoo.pl.entity;

import java.util.Collection;

public class EntityUpdateResult<E extends EntityType<E>, ID extends Identifier<E>> extends EntityChangeResult<E, ID, UpdateEntityCommand<E, ID>> {
    public EntityUpdateResult(UpdateEntityCommand<E, ID> command) {
        super(command);
    }

    public EntityUpdateResult(UpdateEntityCommand<E, ID> command, Collection<ValidationError> errors) {
        super(command, errors);
    }

    @Override
    public ID getIdentifier() {
        return getCommand().getIdentifier();
    }
}
