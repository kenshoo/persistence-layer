package com.kenshoo.pl.one2many.relatedByPK;

import com.kenshoo.jooq.AbstractDataTable;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;

public class ParentTable extends AbstractDataTable<ParentTable> {

    public static final ParentTable INSTANCE = new ParentTable("ParentTable");

    public final TableField<Record, Integer> id = createPKField("id", SQLDataType.INTEGER);
    public final TableField<Record, Integer> idInTarget = createField("idInTarget", SQLDataType.INTEGER);
    public final TableField<Record, String> name = createField("name", SQLDataType.VARCHAR(40));
    public final TableField<Record, String> enum_field = createField("enum_field", SQLDataType.VARCHAR(40));

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
