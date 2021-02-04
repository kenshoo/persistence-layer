package com.kenshoo.pl.transaction;

import com.kenshoo.jooq.AbstractDataTable;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;

public class Table1 extends AbstractDataTable<Table1> {

    public static final Table1 INSTANCE = new Table1("Table1");

    public final TableField<Record, Integer> id = createPKField("id", SQLDataType.INTEGER);
    public final TableField<Record, String> name = createField("name", SQLDataType.VARCHAR(40));

    public Table1(String name) {
        super(name);
    }
    public Table1(Table1 aliased, String alias) {
        super(aliased, alias);
    }

    @Override
    public Table1 as(String alias) {
        return new Table1(this, alias);
    }
}
