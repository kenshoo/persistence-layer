package com.kenshoo.pl.entity;

public class EntityDeleteResult<E extends EntityType<E>, ID extends Identifier<E>> extends EntityChangeResult<E, ID, DeleteEntityCommand<E, ID>> {
    public EntityDeleteResult(DeleteEntityCommand<E, ID> command) {
        super(command);
    }

    public EntityDeleteResult(DeleteEntityCommand<E, ID> command, Iterable<ValidationError> errors) {
        super(command, errors);
    }

    @Override
    public ID getIdentifier() {
        return getCommand().getIdentifier();
    }
}
