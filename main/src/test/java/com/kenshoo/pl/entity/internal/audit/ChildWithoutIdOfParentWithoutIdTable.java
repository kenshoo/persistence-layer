package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.jooq.AbstractDataTable;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;

public class ChildWithoutIdOfParentWithoutIdTable extends AbstractDataTable<ChildWithoutIdOfParentWithoutIdTable> {

    public static final ChildWithoutIdOfParentWithoutIdTable INSTANCE = new ChildWithoutIdOfParentWithoutIdTable();

    public final TableField<Record, String> parent_name = createPKAndFKField("parent_name", SQLDataType.VARCHAR(50), MainWithoutIdTable.INSTANCE.name);
    public final TableField<Record, String> name = createPKField("name", SQLDataType.VARCHAR(50));
    public final TableField<Record, String> desc = createField("desc", SQLDataType.VARCHAR(50));

    private ChildWithoutIdOfParentWithoutIdTable() {
        super("child_without_id_of_parent_without_id");
    }

    private ChildWithoutIdOfParentWithoutIdTable(final ChildWithoutIdOfParentWithoutIdTable aliased, final String alias) {
        super(aliased, alias);
    }

    @Override
    public ChildWithoutIdOfParentWithoutIdTable as(String alias) {
        return new ChildWithoutIdOfParentWithoutIdTable(this, alias);
    }
}
