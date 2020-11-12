package com.kenshoo.pl.data;

import org.jooq.TableField;

/**
 *
 */
public class SingleDBId<T> extends DatabaseId {

    public SingleDBId(TableField<?, T> tableField, T id) {
        super(new TableField[] {tableField}, new Object[]{id});
    }
}
