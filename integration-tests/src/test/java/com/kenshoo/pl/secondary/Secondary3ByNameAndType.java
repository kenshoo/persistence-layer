package com.kenshoo.pl.secondary;

import com.kenshoo.jooq.AbstractDataTable;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;

public class Secondary3ByNameAndType extends AbstractDataTable<Secondary3ByNameAndType> {

    public static final Secondary3ByNameAndType INSTANCE = new Secondary3ByNameAndType("Secondary3ByNameAndType");

    final TableField<Record, Integer> id3 = createPKField("id", SQLDataType.INTEGER.identity(true));
    final TableField<Record, String> name3 = createFKField("name", MainTable.INSTANCE.name);
    final TableField<Record, String> type3 = createFKField("type", MainTable.INSTANCE.type);
    final TableField<Record, String> location3 = createField("location", SQLDataType.VARCHAR(40));

    public Secondary3ByNameAndType(String name3) {
        super(name3);
    }

    public Secondary3ByNameAndType(Secondary3ByNameAndType aliased, String alias) {
        super(aliased, alias);
    }

    @Override
    public Secondary3ByNameAndType as(String alias) {
        return new Secondary3ByNameAndType(this, alias);
    }
}
