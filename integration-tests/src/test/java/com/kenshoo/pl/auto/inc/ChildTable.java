package com.kenshoo.pl.auto.inc;

import com.kenshoo.jooq.AbstractDataTable;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;

public class ChildTable extends AbstractDataTable<ChildTable> {

    public static final ChildTable INSTANCE = new ChildTable("ChildTable");

    final TableField<Record, Integer> parent_id = createPKAndFKField("parent_id", SQLDataType.INTEGER, ParentTable.INSTANCE.id);
    final TableField<Record, Integer> ordinal = createPKField("ordinal", SQLDataType.INTEGER);
    final TableField<Record, String> field1 = createField("field1", SQLDataType.VARCHAR(64));

    public ChildTable(String name) {
        super(name);
    }
    public ChildTable(ChildTable aliased, String alias) {
        super(aliased, alias);
    }

    @Override
    public ChildTable as(String alias) {
        return new ChildTable(this, alias);
    }
}
