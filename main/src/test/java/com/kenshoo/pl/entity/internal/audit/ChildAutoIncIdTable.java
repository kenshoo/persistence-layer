package com.kenshoo.pl.entity.internal.audit;

import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;

public class ChildAutoIncIdTable extends AbstractChildTable {

    public static final ChildAutoIncIdTable INSTANCE = new ChildAutoIncIdTable();

    public final TableField<Record, Long> id = createPKField("id", SQLDataType.BIGINT.identity(true));
    public final TableField<Record, Long> parent_id = createFKField("parent_id", MainAutoIncIdTable.INSTANCE.id);

    private ChildAutoIncIdTable() {
        super();
    }

    private ChildAutoIncIdTable(final ChildAutoIncIdTable aliased, final String alias) {
        super(aliased, alias);
    }

    @Override
    public ChildAutoIncIdTable as(String alias) {
        return new ChildAutoIncIdTable(this, alias);
    }
}
