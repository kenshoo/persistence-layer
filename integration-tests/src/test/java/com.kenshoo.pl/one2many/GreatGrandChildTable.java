package com.kenshoo.pl.one2many;

import com.kenshoo.jooq.AbstractDataTable;
import com.kenshoo.pl.one2many.relatedByPK.GrandChildTable;
import com.kenshoo.pl.one2many.relatedByPK.ParentTable;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;

public class GreatGrandChildTable extends AbstractDataTable<GreatGrandChildTable> {

    public static final GreatGrandChildTable INSTANCE = new GreatGrandChildTable();

    private GreatGrandChildTable() {
        super("greatGrandChildTable");
    }

    public final TableField<Record, Integer> parent_id = createPKAndFKField("parent_id", SQLDataType.INTEGER, ParentTable.INSTANCE.id);
    public final TableField<Record, String> grandchild_color = createPKAndFKField("grandchild_color", SQLDataType.VARCHAR.length(10), GrandChildTable.INSTANCE.color);
    public final TableField<Record, String>  name = createPKField("name", SQLDataType.VARCHAR.length(40).identity(true));

    @Override
    public GreatGrandChildTable as(String alias) {
        return null;
    }

}
