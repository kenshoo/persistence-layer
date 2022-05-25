package com.kenshoo.pl.entity.internal.audit;

import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;

public class MainAutoIncIdTable extends AbstractMainTable {

    public static final MainAutoIncIdTable INSTANCE = new MainAutoIncIdTable();

    public final TableField<Record, Long> id = createPKField("id", SQLDataType.BIGINT.identity(true));

    private MainAutoIncIdTable() {
        super();
    }

    private MainAutoIncIdTable(final MainAutoIncIdTable aliased, final String alias) {
        super(aliased, alias);
    }

    @Override
    public MainAutoIncIdTable as(String alias) {
        return new MainAutoIncIdTable(this, alias);
    }
}
