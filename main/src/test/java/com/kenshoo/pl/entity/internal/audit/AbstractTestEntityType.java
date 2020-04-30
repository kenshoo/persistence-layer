package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.AbstractEntityType;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.SupportedChangeOperation;

public abstract class AbstractTestEntityType<E extends EntityType<E>> extends AbstractEntityType<E> {

    public AbstractTestEntityType(final String name) {
        super(name);
    }

    @Override
    public DataTable getPrimaryTable() {
        return TestEntityTable.INSTANCE;
    }

    public SupportedChangeOperation getSupportedOperation() {
        return SupportedChangeOperation.CREATE_UPDATE_AND_DELETE;
    }
}
