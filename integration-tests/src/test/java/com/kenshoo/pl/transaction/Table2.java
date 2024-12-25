package com.kenshoo.pl.transaction;

import com.kenshoo.jooq.AbstractDataTable;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;

public class Table2 extends AbstractDataTable<Table2> {

    public static final Table2 INSTANCE = new Table2("Table2");

    public final TableField<Record, Integer> id = createPKField("id", SQLDataType.INTEGER.identity(true));
    public final TableField<Record, String> name = createField("name", SQLDataType.VARCHAR(40));

    public Table2(String name) {
        super(name);
    }
    public Table2(Table2 aliased, String alias) {
        super(aliased, alias);
    }

    @Override
    public Table2 as(String alias) {
        return new Table2(this, alias);
    }
}
