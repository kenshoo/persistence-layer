package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.AbstractEntityType;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.SupportedChangeOperation;
import com.kenshoo.pl.entity.internal.audit.ChildAutoIncIdTable;

abstract class AbstractAutoIncIdChildType<E extends EntityType<E>> extends AbstractEntityType<E> {

    AbstractAutoIncIdChildType(final String name) {
        super(name);
    }

    @Override
    public DataTable getPrimaryTable() {
        return ChildAutoIncIdTable.INSTANCE;
    }

    public SupportedChangeOperation getSupportedOperation() {
        return SupportedChangeOperation.CREATE_UPDATE_AND_DELETE;
    }
}
