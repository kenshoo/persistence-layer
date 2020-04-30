package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.jooq.AbstractDataTable;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;

public class TestEntityTable extends AbstractDataTable<TestEntityTable> {

    public static final TestEntityTable INSTANCE = new TestEntityTable("test_entity");

    public final TableField<Record, Long> id = createPKField("id", SQLDataType.BIGINT.identity(true));
    public final TableField<Record, String> name = createField("name", SQLDataType.VARCHAR(50));
    public final TableField<Record, String> desc = createField("desc", SQLDataType.VARCHAR(50));
    public final TableField<Record, String> desc2 = createField("desc2", SQLDataType.VARCHAR(50));

    private TestEntityTable(final String name) {
        super(name);
    }

    private TestEntityTable(final TestEntityTable aliased, final String alias) {
        super(aliased, alias);
    }

    @Override
    public TestEntityTable as(String alias) {
        return new TestEntityTable(this, alias);
    }
}
