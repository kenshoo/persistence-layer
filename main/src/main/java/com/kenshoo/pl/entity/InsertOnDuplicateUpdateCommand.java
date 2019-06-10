package com.kenshoo.pl.entity;

public class InsertOnDuplicateUpdateCommand<E extends EntityType<E>, ID extends Identifier<E>> extends UpdateEntityCommand<E,ID> {

    private ChangeOperation changeOperation = ChangeOperation.UPDATE;
    private boolean allowMissingEntity = true;

    public InsertOnDuplicateUpdateCommand(E entityType, ID key) {
        super(entityType, key);
    }

    @Override
    public ChangeOperation getChangeOperation() {
        return changeOperation;
    }

    @Override
    public boolean allowMissingEntity() {
        return allowMissingEntity;
    }

    @Override
    public void updateOperator(ChangeOperation changeOperation) {
        if(this.changeOperation == ChangeOperation.UPDATE && changeOperation == ChangeOperation.CREATE) {
            this.changeOperation = changeOperation;
            this.allowMissingEntity = false;
            ChangeEntityCommand.copy(this, this.getIdentifier());
        } else {
            throw new UnsupportedOperationException("Unsupported update change operation from " + this.changeOperation + " to " + changeOperation);
        }
    }
}
