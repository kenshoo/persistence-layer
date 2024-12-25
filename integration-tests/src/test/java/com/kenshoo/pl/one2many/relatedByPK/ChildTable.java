package com.kenshoo.pl.one2many.relatedByPK;

import com.kenshoo.jooq.AbstractDataTable;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;

public class ChildTable extends AbstractDataTable<ChildTable> {

    public static final ChildTable INSTANCE = new ChildTable("ChildTable");

    public final TableField<Record, Integer> parent_id = createFKField("parent_id", ParentTable.INSTANCE.id);
    public final TableField<Record, Integer> ordinal = createField("ordinal", SQLDataType.INTEGER);
    public final TableField<Record, String> field1 = createField("field1", SQLDataType.VARCHAR(64));
    public final TableField<Record, Integer> id = createPKField("id", SQLDataType.INTEGER.identity(true));

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
