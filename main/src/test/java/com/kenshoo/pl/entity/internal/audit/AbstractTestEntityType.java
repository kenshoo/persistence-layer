package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.AbstractEntityType;
import com.kenshoo.pl.entity.EntityType;

abstract class AbstractTestEntityType<E extends EntityType<E>> extends AbstractEntityType<E> {

    AbstractTestEntityType(final String name) {
        super(name);
    }

    @Override
    public DataTable getPrimaryTable() {
        return TestEntityTable.INSTANCE;
    }
}
