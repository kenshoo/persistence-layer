package com.kenshoo.pl.entity;

import com.kenshoo.jooq.AbstractDataTable;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;

public class ChildForTestTable extends AbstractDataTable<ChildForTestTable> {

    public static final ChildForTestTable INSTANCE = new ChildForTestTable("ChildForTest");

    final TableField<Record, Integer> id = createPKField("id", SQLDataType.INTEGER.identity(true));
    final TableField<Record, String> field = createPKField("field", SQLDataType.VARCHAR(50).nullable(false));
    final TableField<Record, Integer> parent_id = createFKField("parent_id", EntityForTestTable.INSTANCE.id);

    public ChildForTestTable(String name) {
        super(name);
    }

    public ChildForTestTable(ChildForTestTable aliased, String alias) {
        super(aliased, alias);
    }

    @Override
    public ChildForTestTable as(String alias) {
        return new ChildForTestTable(this, alias);
    }
}
