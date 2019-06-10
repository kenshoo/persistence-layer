package com.kenshoo.jooq;

import org.jooq.DSLContext;

import java.util.Collection;

public class StringIdsList extends IdsListImpl<String> implements IdsList<String> {

    public StringIdsList(DSLContext dslContext) {
        super(dslContext, IdsTempTable.STRING_TABLE);
    }

    public StringIdsList(DSLContext dslContext, Collection<String> ids) {
        super(dslContext, IdsTempTable.STRING_TABLE, ids);
    }
}
