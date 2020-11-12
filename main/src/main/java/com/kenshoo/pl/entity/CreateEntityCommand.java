package com.kenshoo.pl.entity;

public class CreateEntityCommand<E extends EntityType<E>> extends ChangeEntityCommand<E> {

    private Identifier<E> identifier;

    public CreateEntityCommand(E entityType) {
        super(entityType);
    }

    @Override
    public Identifier<E> getIdentifier() {
        return identifier;
    }

    @Override
    public ChangeOperation getChangeOperation() {
        return ChangeOperation.CREATE;
    }

    public void setIdentifier(Identifier<E> identifier) {
        this.identifier = identifier;
    }
}
