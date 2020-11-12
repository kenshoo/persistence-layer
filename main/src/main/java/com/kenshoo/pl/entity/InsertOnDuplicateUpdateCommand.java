package com.kenshoo.pl.entity;

import static com.kenshoo.pl.entity.ChangeOperation.CREATE;
import static com.kenshoo.pl.entity.ChangeOperation.UPDATE;

public class InsertOnDuplicateUpdateCommand<E extends EntityType<E>, ID extends Identifier<E>> extends UpdateEntityCommand<E,ID> {

    private ChangeOperation changeOperation = UPDATE;
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
        if (this.changeOperation == UPDATE && changeOperation == CREATE) {
            this.changeOperation = changeOperation;
            this.allowMissingEntity = false;
            ChangeEntityCommand.copy(this, this.getIdentifier());
            this.getChildren().forEach(child -> child.updateOperator(changeOperation));
        } else {
            throw new UnsupportedOperationException("Unsupported update change operation from " + this.changeOperation + " to " + changeOperation);
        }
    }
}
