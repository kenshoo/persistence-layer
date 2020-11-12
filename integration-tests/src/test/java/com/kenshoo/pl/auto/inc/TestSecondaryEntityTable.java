package com.kenshoo.pl.auto.inc;

import com.kenshoo.jooq.AbstractDataTable;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;

public class TestSecondaryEntityTable extends AbstractDataTable<TestSecondaryEntityTable> {

    public static final TestSecondaryEntityTable INSTANCE = new TestSecondaryEntityTable("TestSecondaryEntity");

    public final TableField<Record, Integer> id = createPKField("id", SQLDataType.INTEGER.identity(true));
    public final TableField<Record, Integer> parentId = createFKField("parent_id", TestEntityTable.INSTANCE.id);
    public final TableField<Record, String> secondName = createField("second_name", SQLDataType.VARCHAR.length(100));

    private TestSecondaryEntityTable(final String name) {
        super(name);
    }

    private TestSecondaryEntityTable(final TestSecondaryEntityTable aliased, final String alias) {
        super(aliased, alias);
    }

    @Override
    public TestSecondaryEntityTable as(String alias) {
        return new TestSecondaryEntityTable(this, alias);
    }
}
