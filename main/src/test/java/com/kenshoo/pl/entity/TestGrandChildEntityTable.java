package com.kenshoo.pl.entity;

import com.kenshoo.jooq.AbstractDataTable;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;

public class TestGrandChildEntityTable extends AbstractDataTable<TestGrandChildEntityTable> {

    public static final TestGrandChildEntityTable TABLE = new TestGrandChildEntityTable();

    private TestGrandChildEntityTable() {
        super("testGrandChildTable");
    }

    public final TableField<Record, Integer> id = createPKField("id", SQLDataType.INTEGER);
    public final TableField<Record, Integer> ordinal = createFKField("ordinal", TestChildEntityTable.TABLE.ordinal);
    public final TableField<Record, String>  grand_child_field_1 = createField("field_1", SQLDataType.VARCHAR.length(10));
    public final TableField<Record, String>  grand_child_field_2 = createField("field_2", SQLDataType.VARCHAR.length(10));
    public final TableField<Record, Integer> grand_child_field_3 = createField("field_3", SQLDataType.INTEGER);

    @Override
    public TestGrandChildEntityTable as(String alias) {
        return null;
    }

}
