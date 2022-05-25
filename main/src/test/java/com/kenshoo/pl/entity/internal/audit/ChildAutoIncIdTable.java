package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.jooq.AbstractDataTable;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;

public class ChildAutoIncIdTable extends AbstractDataTable<ChildAutoIncIdTable> {

    public static final ChildAutoIncIdTable INSTANCE = new ChildAutoIncIdTable();

    public final TableField<Record, Long> id = createPKField("id", SQLDataType.BIGINT.identity(true));
    public final TableField<Record, Long> parent_id = createFKField("parent_id", MainAutoIncIdTable.INSTANCE.id);
    public final TableField<Record, String> name = createField("name", SQLDataType.VARCHAR(50));
    public final TableField<Record, String> desc = createField("desc", SQLDataType.VARCHAR(50));

    private ChildAutoIncIdTable() {
        super("child_auto_inc_id");
    }

    private ChildAutoIncIdTable(final ChildAutoIncIdTable aliased, final String alias) {
        super(aliased, alias);
    }

    @Override
    public ChildAutoIncIdTable as(String alias) {
        return new ChildAutoIncIdTable(this, alias);
    }
}
