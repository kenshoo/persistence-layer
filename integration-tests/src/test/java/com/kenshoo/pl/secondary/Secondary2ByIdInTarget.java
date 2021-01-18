package com.kenshoo.pl.secondary;

import com.kenshoo.jooq.AbstractDataTable;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;

public class Secondary2ByIdInTarget extends AbstractDataTable<Secondary2ByIdInTarget> {

    public static final Secondary2ByIdInTarget INSTANCE = new Secondary2ByIdInTarget("Secondary2ByIdInTarget");

    final TableField<Record, Integer> id2 = createPKField("id", SQLDataType.INTEGER.identity(true));
    final TableField<Record, Integer> id_in_target2 = createFKField("id_in_target", MainTable.INSTANCE.id_in_target);
    final TableField<Record, String> url2 = createField("url", SQLDataType.VARCHAR.length(100).nullable(false));
    final TableField<Record, String> url_param2 = createField("url_param", SQLDataType.VARCHAR.length(100));

    public Secondary2ByIdInTarget(String name) {
        super(name);
    }

    public Secondary2ByIdInTarget(Secondary2ByIdInTarget aliased, String alias) {
        super(aliased, alias);
    }

    @Override
    public Secondary2ByIdInTarget as(String alias) {
        return new Secondary2ByIdInTarget(this, alias);
    }
}
