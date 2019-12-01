package com.kenshoo.pl.auto.inc;

import com.kenshoo.jooq.AbstractDataTable;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;

public class GrandParentTable extends AbstractDataTable<GrandParentTable> {

    public static final GrandParentTable INSTANCE = new GrandParentTable("ParentTable");

    final TableField<Record, Integer> id = createPKField("id", SQLDataType.INTEGER);

    public GrandParentTable(String name) {
        super(name);
    }
    public GrandParentTable(GrandParentTable aliased, String alias) {
        super(aliased, alias);
    }

    @Override
    public GrandParentTable as(String alias) {
        return new GrandParentTable(this, alias);
    }
}
