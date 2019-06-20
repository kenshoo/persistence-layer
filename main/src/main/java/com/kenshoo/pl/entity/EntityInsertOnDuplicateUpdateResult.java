package com.kenshoo.pl.entity;


public class EntityInsertOnDuplicateUpdateResult<E extends EntityType<E>, ID extends Identifier<E>> extends EntityChangeResult<E, ID, InsertOnDuplicateUpdateCommand<E, ID>> {

    public EntityInsertOnDuplicateUpdateResult(InsertOnDuplicateUpdateCommand<E, ID> command) {
        super(command);
    }

    public EntityInsertOnDuplicateUpdateResult(InsertOnDuplicateUpdateCommand<E, ID> command, Iterable<ValidationError> errors) {
        super(command, errors);
    }

    @Override
    public ID getIdentifier() {
        return getCommand().getIdentifier();
    }
}
