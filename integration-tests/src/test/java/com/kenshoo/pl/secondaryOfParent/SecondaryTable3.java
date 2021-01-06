package com.kenshoo.pl.secondaryOfParent;

import com.kenshoo.jooq.AbstractDataTable;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;

public class SecondaryTable3 extends AbstractDataTable<SecondaryTable3> {

    public static final SecondaryTable3 INSTANCE = new SecondaryTable3("secondary3");

    final TableField<Record, Integer> id = createPKField("id", SQLDataType.INTEGER.identity(true));
    final TableField<Record, String> name = createFKField("name", MainTable.INSTANCE.name);
    final TableField<Record, String> type = createFKField("type", MainTable.INSTANCE.type);
    final TableField<Record, String> location = createField("location", SQLDataType.VARCHAR(40));

    public SecondaryTable3(String name) {
        super(name);
    }

    public SecondaryTable3(SecondaryTable3 aliased, String alias) {
        super(aliased, alias);
    }

    @Override
    public SecondaryTable3 as(String alias) {
        return new SecondaryTable3(this, alias);
    }
}
