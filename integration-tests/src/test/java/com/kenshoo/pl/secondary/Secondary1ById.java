package com.kenshoo.pl.secondary;

import com.kenshoo.jooq.AbstractDataTable;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;

public class Secondary1ById extends AbstractDataTable<Secondary1ById> {

    public static final Secondary1ById INSTANCE = new Secondary1ById("Secondary1ById");

    final TableField<Record, Integer> id1 = createPKAndFKField("id", SQLDataType.INTEGER, MainTable.INSTANCE.id);
    final TableField<Record, Double> budget1 = createField("budget", SQLDataType.DOUBLE);

    public Secondary1ById(String name) {
        super(name);
    }

    public Secondary1ById(Secondary1ById aliased, String alias) {
        super(aliased, alias);
    }

    @Override
    public Secondary1ById as(String alias) {
        return new Secondary1ById(this, alias);
    }
}
