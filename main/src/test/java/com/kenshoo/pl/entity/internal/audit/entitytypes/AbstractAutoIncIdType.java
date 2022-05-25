package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.AbstractEntityType;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.SupportedChangeOperation;
import com.kenshoo.pl.entity.internal.audit.MainAutoIncIdTable;

public abstract class AbstractAutoIncIdType<E extends EntityType<E>> extends AbstractEntityType<E> {

    public AbstractAutoIncIdType(final String name) {
        super(name);
    }

    @Override
    public DataTable getPrimaryTable() {
        return MainAutoIncIdTable.INSTANCE;
    }

    public SupportedChangeOperation getSupportedOperation() {
        return SupportedChangeOperation.CREATE_UPDATE_AND_DELETE;
    }
}
