package com.kenshoo.pl.auto.inc;

import com.kenshoo.jooq.AbstractDataTable;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;

public class TestEntityTable extends AbstractDataTable<TestEntityTable> {

    public static final TestEntityTable INSTANCE = new TestEntityTable("TestEntity");

    final public TableField<Record, Integer> id = createPKField("id", SQLDataType.INTEGER.identity(true));
    final public TableField<Record, String> name = createField("name", SQLDataType.VARCHAR.length(50));
    final public TableField<Record, String> field1 = createField("field1", SQLDataType.VARCHAR.length(50));

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
