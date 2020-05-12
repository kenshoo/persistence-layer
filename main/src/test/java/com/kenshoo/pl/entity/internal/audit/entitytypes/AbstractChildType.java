package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.AbstractEntityType;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.SupportedChangeOperation;
import com.kenshoo.pl.entity.internal.audit.ChildTable;

abstract class AbstractChildType<E extends EntityType<E>> extends AbstractEntityType<E> {

    AbstractChildType(final String name) {
        super(name);
    }

    @Override
    public DataTable getPrimaryTable() {
        return ChildTable.INSTANCE;
    }

    public SupportedChangeOperation getSupportedOperation() {
        return SupportedChangeOperation.CREATE_UPDATE_AND_DELETE;
    }
}
