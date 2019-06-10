package com.kenshoo.jooq;

import org.jooq.DSLContext;

import java.util.Collection;

public class LongIdsList extends IdsListImpl<Long> implements IdsList<Long> {

    public LongIdsList(DSLContext dslContext) {
        super(dslContext, IdsTempTable.LONG_TABLE);
    }

    public LongIdsList(DSLContext dslContext, Collection<Long> ids) {
        super(dslContext, IdsTempTable.LONG_TABLE, ids);
    }
}
