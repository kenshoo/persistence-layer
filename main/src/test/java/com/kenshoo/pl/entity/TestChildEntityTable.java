package com.kenshoo.pl.entity;

import com.kenshoo.jooq.AbstractDataTable;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;

public class TestChildEntityTable extends AbstractDataTable<TestChildEntityTable> {

    public static final TestChildEntityTable TABLE = new TestChildEntityTable();

    private TestChildEntityTable() {
        super("testChildTable");
    }

    public final TableField<Record, Integer> ordinal = createField("ordinal", SQLDataType.INTEGER);
    public final TableField<Record, Integer> parent_id = createFKField("parent_id", TestEntityTable.TABLE.id);
    public final TableField<Record, String> child_field_1 = createField("field_1", SQLDataType.VARCHAR.length(10));
    public final TableField<Record, String> child_field_2 = createField("field_2", SQLDataType.VARCHAR.length(10));
    public final TableField<Record, Integer> child_field_3 = createField("field_3", SQLDataType.INTEGER);

    @Override
    public TestChildEntityTable as(String alias) {
        return null;
    }

}
