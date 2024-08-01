package com.kenshoo.pl.secondary;

import com.kenshoo.jooq.AbstractDataTable;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;

public class MainTable extends AbstractDataTable<MainTable> {

    public static final MainTable INSTANCE = new MainTable("main");

    final TableField<Record, Integer> id = createPKField("id", SQLDataType.INTEGER.identity(true));
    final TableField<Record, Integer> id_in_target = createField("id_in_target", SQLDataType.INTEGER);
    final TableField<Record, String> name = createField("name", SQLDataType.VARCHAR.length(50));
    final TableField<Record, String> type = createField("type", SQLDataType.VARCHAR.length(10));

    public MainTable(String name) {
        super(name);
    }

    public MainTable(MainTable aliased, String alias) {
        super(aliased, alias);
    }

    @Override
    public MainTable as(String alias) {
        return new MainTable(this, alias);
    }
}
