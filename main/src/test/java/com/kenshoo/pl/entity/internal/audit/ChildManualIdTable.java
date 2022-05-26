package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.jooq.AbstractDataTable;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;

public class ChildManualIdTable extends AbstractDataTable<ChildManualIdTable> {

    public static final ChildManualIdTable INSTANCE = new ChildManualIdTable();

    public final TableField<Record, Long> id = createPKField("id", SQLDataType.BIGINT);
    public final TableField<Record, Long> parent_id = createFKField("parent_id", MainManualIdTable.INSTANCE.id);
    public final TableField<Record, String> name = createField("name", SQLDataType.VARCHAR(50));
    public final TableField<Record, String> desc = createField("desc", SQLDataType.VARCHAR(50));

    private ChildManualIdTable() {
        super("child_manual_id");
    }

    private ChildManualIdTable(final ChildManualIdTable aliased, final String alias) {
        super(aliased, alias);
    }

    @Override
    public ChildManualIdTable as(String alias) {
        return new ChildManualIdTable(this, alias);
    }
}
