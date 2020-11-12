package com.kenshoo.pl.entity;

import com.google.common.base.Preconditions;

public class UpdateEntityCommand<E extends EntityType<E>, ID extends Identifier<E>> extends ChangeEntityCommand<E> {

    private final ID key;

    public UpdateEntityCommand(E entityType, ID key) {
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
        return ChangeOperation.UPDATE;
    }
}
