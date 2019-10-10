package com.kenshoo.pl.entity;

import com.kenshoo.jooq.AbstractDataTable;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;

public class TestEntityAutoIncTable extends AbstractDataTable<TestEntityAutoIncTable> {

    public static final TestEntityAutoIncTable TABLE = new TestEntityAutoIncTable();

    private TestEntityAutoIncTable() {
        super("testAutoIncTable");
    }

    public final TableField<Record, Integer> id = createPKField("id", SQLDataType.INTEGER.identity(true));
    public final TableField<Record, String> field_1 = createField("field_1", SQLDataType.VARCHAR.length(10));

    @Override
    public TestEntityAutoIncTable as(String alias) {
        return null;
    }
}
