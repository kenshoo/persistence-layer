package com.kenshoo.pl.secondaryOfParent;

import com.kenshoo.jooq.AbstractDataTable;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;

public class SecondaryTable2 extends AbstractDataTable<SecondaryTable2> {

    public static final SecondaryTable2 INSTANCE = new SecondaryTable2("secondary1");

    final TableField<Record, Integer> id = createPKField("id", SQLDataType.INTEGER.identity(true));
    final TableField<Record, Integer> id_in_target = createFKField("id_in_target", MainTable.INSTANCE.id_in_target);
    final TableField<Record, String> url = createField("url", SQLDataType.VARCHAR.length(100).nullable(false));
    final TableField<Record, String> url_param = createField("url_param", SQLDataType.VARCHAR.length(100));

    public SecondaryTable2(String name) {
        super(name);
    }

    public SecondaryTable2(SecondaryTable2 aliased, String alias) {
        super(aliased, alias);
    }

    @Override
    public SecondaryTable2 as(String alias) {
        return new SecondaryTable2(this, alias);
    }
}
