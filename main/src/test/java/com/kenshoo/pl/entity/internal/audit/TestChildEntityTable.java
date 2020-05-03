package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.jooq.AbstractDataTable;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;

public class TestChildEntityTable extends AbstractDataTable<TestChildEntityTable> {

    public static final TestChildEntityTable INSTANCE = new TestChildEntityTable("test_child_entity");

    public final TableField<Record, Long> id = createPKField("id", SQLDataType.BIGINT.identity(true));
    public final TableField<Record, Long> parent_id = createFKField("parent_id", TestEntityTable.INSTANCE.id);
    public final TableField<Record, String> name = createField("name", SQLDataType.VARCHAR(50));
    public final TableField<Record, String> desc = createField("desc", SQLDataType.VARCHAR(50));

    private TestChildEntityTable(final String name) {
        super(name);
    }

    private TestChildEntityTable(final TestChildEntityTable aliased, final String alias) {
        super(aliased, alias);
    }

    @Override
    public TestChildEntityTable as(String alias) {
        return new TestChildEntityTable(this, alias);
    }
}
