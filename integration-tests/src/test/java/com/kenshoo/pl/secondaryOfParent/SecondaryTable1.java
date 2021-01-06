package com.kenshoo.pl.secondaryOfParent;

import com.kenshoo.jooq.AbstractDataTable;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;

public class SecondaryTable1 extends AbstractDataTable<SecondaryTable1> {

    public static final SecondaryTable1 INSTANCE = new SecondaryTable1("secondary2");

    final TableField<Record, Integer> id = createPKAndFKField("id", SQLDataType.INTEGER, MainTable.INSTANCE.id);
    final TableField<Record, Double> budget = createField("budget", SQLDataType.DOUBLE);

    public SecondaryTable1(String name) {
        super(name);
    }

    public SecondaryTable1(SecondaryTable1 aliased, String alias) {
        super(aliased, alias);
    }

    @Override
    public SecondaryTable1 as(String alias) {
        return new SecondaryTable1(this, alias);
    }
}
