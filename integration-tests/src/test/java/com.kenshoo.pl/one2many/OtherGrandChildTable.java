package com.kenshoo.pl.one2many;

import com.kenshoo.jooq.AbstractDataTable;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;

public class OtherGrandChildTable extends AbstractDataTable<OtherGrandChildTable> {

    public static final OtherGrandChildTable INSTANCE = new OtherGrandChildTable();

    private OtherGrandChildTable() {
        super("otherGrandChildTable");
    }

    public final TableField<Record, Integer> parent_id = createPKAndFKField("parent_id", SQLDataType.INTEGER, ParentTable.INSTANCE.id);
    public final TableField<Record, Integer> child_id = createPKAndFKField("child_id", SQLDataType.INTEGER, ChildTable.INSTANCE.id);
    public final TableField<Record, String>  name = createPKField("name", SQLDataType.VARCHAR.length(40));

    @Override
    public OtherGrandChildTable as(String alias) {
        return null;
    }

}
