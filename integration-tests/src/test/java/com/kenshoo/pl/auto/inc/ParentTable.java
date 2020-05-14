package com.kenshoo.pl.auto.inc;

import com.kenshoo.jooq.AbstractDataTable;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;

public class ParentTable extends AbstractDataTable<ParentTable> {

    public static final ParentTable INSTANCE = new ParentTable("ParentTable");

    final TableField<Record, Integer> id = createPKField("id", SQLDataType.INTEGER.identity(true));
    final TableField<Record, Integer> idInTarget = createField("idInTarget", SQLDataType.INTEGER);
    final TableField<Record, String> name = createField("name", SQLDataType.VARCHAR(40));
    final TableField<Record, Integer> grand_parent_id = createFKField("grand_parent_id", GrandParentTable.INSTANCE.id);

    public ParentTable(String name) {
        super(name);
    }
    public ParentTable(ParentTable aliased, String alias) {
        super(aliased, alias);
    }

    @Override
    public ParentTable as(String alias) {
        return new ParentTable(this, alias);
    }
}
