package com.kenshoo.pl.one2many.relatedByNonPK;

import com.kenshoo.jooq.AbstractDataTable;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;

public class ChildTable extends AbstractDataTable<ChildTable> {

    public static final ChildTable INSTANCE = new ChildTable("ChildTable");

    public final TableField<Record, Integer> id = createPKField("id", SQLDataType.INTEGER.identity(true));
    public final TableField<Record, String> type = createFKField("type", ParentTable.INSTANCE.type);
    public final TableField<Record, Integer> idInTarget = createFKField("idInTarget", ParentTable.INSTANCE.idInTarget);
    public final TableField<Record, Integer> ordinal = createField("ordinal", SQLDataType.INTEGER);
    public final TableField<Record, String> field1 = createField("field1", SQLDataType.VARCHAR(64));

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
