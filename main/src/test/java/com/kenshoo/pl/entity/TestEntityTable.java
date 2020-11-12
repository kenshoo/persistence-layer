package com.kenshoo.pl.entity;

import com.kenshoo.jooq.AbstractDataTable;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;

/**
 * Created by dimag on 13/01/2016.
 */
public class TestEntityTable extends AbstractDataTable<TestEntityTable> {

    public static final TestEntityTable TABLE = new TestEntityTable();

    private TestEntityTable() {
        super("testTable");
    }

    public final TableField<Record, Integer> id = createPKField("id", SQLDataType.INTEGER);
    public final TableField<Record, String> field_1 = createField("field_1", SQLDataType.VARCHAR.length(10));
    public final TableField<Record, String> field_2 = createField("field_2", SQLDataType.VARCHAR.length(10));
    public final TableField<Record, Integer> field_3 = createField("field_3", SQLDataType.INTEGER);

    @Override
    public TestEntityTable as(String alias) {
        return null;
    }
}
