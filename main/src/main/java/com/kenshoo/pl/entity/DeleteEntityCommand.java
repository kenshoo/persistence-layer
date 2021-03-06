package com.kenshoo.pl.entity;

import com.google.common.base.Preconditions;

public class DeleteEntityCommand<E extends EntityType<E>, ID extends Identifier<E>> extends ChangeEntityCommand<E> {

    private final ID key;
    private boolean isCascade;

    public DeleteEntityCommand(E entityType,  ID key) {
        super(entityType);
        Preconditions.checkArgument(key != null, "key can not be null");
        this.key = key;
    }

    @Override
    public ID getIdentifier() {
        return key;
    }

    @Override
    public ChangeOperation getChangeOperation() {
        return ChangeOperation.DELETE;
    }

    public DeleteEntityCommand<E, ID> setCascade() {
        this.isCascade = true;
        return this;
    }

    public boolean isCascade() {
        return this.isCascade;
    }
}
