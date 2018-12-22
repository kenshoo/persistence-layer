package com.kenshoo.pl.entity;

import java.util.Collection;
import java.util.Collections;

public class EntityCreateResult<E extends EntityType<E>, ID extends Identifier<E>> extends EntityChangeResult<E, ID, CreateEntityCommand<E>> {

    public EntityCreateResult(CreateEntityCommand<E> command) {
        this(command, Collections.emptyList());
    }

    public EntityCreateResult(CreateEntityCommand<E> command, Collection<ValidationError> errors) {
        super(command, errors);
    }

    @Override
    public ID getIdentifier() {
        // You'll have to believe us
        //noinspection unchecked
        return (ID) getCommand().getIdentifier();
    }
}
