package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.jooq.AbstractDataTable;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;

public class MainManualIdTable extends AbstractDataTable<MainManualIdTable> {

    public static final MainManualIdTable INSTANCE = new MainManualIdTable();

    public final TableField<Record, Long> id = createPKField("id", SQLDataType.BIGINT);
    public final TableField<Record, String> name = createField("name", SQLDataType.VARCHAR(50));
    public final TableField<Record, String> desc = createField("desc", SQLDataType.VARCHAR(50));

    private MainManualIdTable() {
        super("main_manual_id");
    }

    private MainManualIdTable(final MainManualIdTable aliased, final String alias) {
        super(aliased, alias);
    }

    @Override
    public MainManualIdTable as(String alias) {
        return new MainManualIdTable(this, alias);
    }
}
