package com.kenshoo.pl.one2many.events;

import com.kenshoo.jooq.AbstractDataTable;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;

public class ChildTable extends AbstractDataTable<ChildTable> {

    public static final ChildTable INSTANCE = new ChildTable("ChildTable");

    public final TableField<Record, Integer> id = createPKField("id", SQLDataType.INTEGER.identity(true));
    public final TableField<Record, Integer> parent_id = createFKField("parent_id", ParentTable.INSTANCE.id);
    public final TableField<Record, String>  child_name = createField("name", SQLDataType.VARCHAR(64));

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
