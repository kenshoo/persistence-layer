package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.jooq.AbstractDataTable;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;

public class ChildTable extends AbstractDataTable<ChildTable> {

    public static final ChildTable INSTANCE = new ChildTable("child");

    public final TableField<Record, Long> id = createPKField("id", SQLDataType.BIGINT.identity(true));
    public final TableField<Record, Long> parent_id = createFKField("parent_id", MainTable.INSTANCE.id);
    public final TableField<Record, String> name = createField("name", SQLDataType.VARCHAR(50));
    public final TableField<Record, String> desc = createField("desc", SQLDataType.VARCHAR(50));

    private ChildTable(final String name) {
        super(name);
    }

    private ChildTable(final ChildTable aliased, final String alias) {
        super(aliased, alias);
    }

    @Override
    public ChildTable as(String alias) {
        return new ChildTable(this, alias);
    }
}
