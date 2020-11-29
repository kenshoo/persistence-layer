package com.kenshoo.pl.one2many.relatedByPK;

import com.kenshoo.jooq.AbstractDataTable;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;

public class OtherChildTable extends AbstractDataTable<OtherChildTable> {

    public static final OtherChildTable INSTANCE = new OtherChildTable("OtherChildTable");

    public final TableField<Record, Integer> parent_id = createFKField("parent_id", ParentTable.INSTANCE.id);
    public final TableField<Record, String> name = createField("name", SQLDataType.VARCHAR(64));
    public final TableField<Record, Integer> id = createPKField("id", SQLDataType.INTEGER);

    public OtherChildTable(String name) {
        super(name);
    }

    public OtherChildTable(OtherChildTable aliased, String alias) {
        super(aliased, alias);
    }

    @Override
    public OtherChildTable as(String alias) {
        return new OtherChildTable(this, alias);
    }
}
