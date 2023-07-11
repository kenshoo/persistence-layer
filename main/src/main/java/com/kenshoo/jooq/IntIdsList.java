package com.kenshoo.jooq;

import org.jooq.DSLContext;

import java.util.Collection;

public class IntIdsList extends IdsListImpl<Integer> implements IdsList<Integer> {

    public IntIdsList(DSLContext dslContext) {
        super(dslContext, IdsTempTable.INT_TABLE);
    }

    public IntIdsList(DSLContext dslContext, Collection<Integer> ids) {
        super(dslContext, IdsTempTable.INT_TABLE, ids);
    }
}
