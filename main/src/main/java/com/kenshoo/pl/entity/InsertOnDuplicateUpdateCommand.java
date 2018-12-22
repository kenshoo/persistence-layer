package com.kenshoo.pl.entity;

import com.kenshoo.pl.entity.spi.FieldValueSupplier;
import com.kenshoo.pl.entity.spi.MultiFieldValueSupplier;

import java.util.Collection;

public class InsertOnDuplicateUpdateCommand<E extends EntityType<E>, ID extends Identifier<E>> extends UpdateEntityCommand<E,ID> {

    private ChangeOperation changeOperation = ChangeOperation.UPDATE;
    private boolean allowMissingEntity = true;

    public InsertOnDuplicateUpdateCommand(E entityType, ID key) {
        super(entityType, key);
    }

    @Override
    public <T> void set(EntityField<E, T> field, FieldValueSupplier<T> valueSupplier) {
        throw new UnsupportedOperationException("Supplier is not supported in insert on duplicate update");
    }

    @Override
    public <T> void set(EntityFieldPrototype<T> fieldPrototype, FieldValueSupplier<T> valueSupplier) {
        throw new UnsupportedOperationException("Supplier is not supported in insert on duplicate update");
    }

    @Override
    public void set(Collection<EntityField<E, ?>> fields, MultiFieldValueSupplier<E> valueSupplier) {
        throw new UnsupportedOperationException("Supplier is not supported in insert on duplicate update");
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
    void updateOperator(ChangeOperation changeOperation) {
        if(this.changeOperation == ChangeOperation.UPDATE && changeOperation == ChangeOperation.CREATE) {
            this.changeOperation = changeOperation;
            this.allowMissingEntity = false;
            ChangeEntityCommand.copy(this, this.getIdentifier());
        } else {
            throw new UnsupportedOperationException("Unsupported update change operation from " + this.changeOperation + " to " + changeOperation);
        }
    }
}
