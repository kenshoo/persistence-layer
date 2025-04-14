package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.jooq.AbstractDataTable;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;


public class ChildWithoutIdOfParentManualIdTable extends AbstractDataTable<ChildWithoutIdOfParentManualIdTable> {

    public static final ChildWithoutIdOfParentManualIdTable INSTANCE = new ChildWithoutIdOfParentManualIdTable();

    public final TableField<Record, Long> parent_id = createPKAndFKField("parent_id", SQLDataType.BIGINT.identity(true), MainManualIdTable.INSTANCE.id);
    public final TableField<Record, String> name = createPKField("name", SQLDataType.VARCHAR(50).nullable(false));
    public final TableField<Record, String> desc = createField("desc", SQLDataType.VARCHAR(50));

    private ChildWithoutIdOfParentManualIdTable() {
        super("child_without_id_of_parent_manual_id");
    }

    private ChildWithoutIdOfParentManualIdTable(final ChildWithoutIdOfParentManualIdTable aliased, final String alias) {
        super(aliased, alias);
    }

    @Override
    public ChildWithoutIdOfParentManualIdTable as(String alias) {
        return new ChildWithoutIdOfParentManualIdTable(this, alias);
    }
}
