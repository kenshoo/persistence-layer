package com.kenshoo.pl.one2many.relatedByPK;

import com.kenshoo.jooq.AbstractDataTable;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;

public class GrandChildTable extends AbstractDataTable<GrandChildTable> {

    public static final GrandChildTable INSTANCE = new GrandChildTable();

    private GrandChildTable() {
        super("GrandChildTable");
    }

    public final TableField<Record, Integer> child_id = createPKAndFKField("child_id", SQLDataType.INTEGER, ChildTable.INSTANCE.id);
    public final TableField<Record, String>  color = createPKField("color", SQLDataType.VARCHAR.length(10));

    @Override
    public GrandChildTable as(String alias) {
        return null;
    }

}
