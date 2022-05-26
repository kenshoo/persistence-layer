package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.jooq.AbstractDataTable;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;

public class MainAutoIncIdTable extends AbstractDataTable<MainAutoIncIdTable> {

    public static final MainAutoIncIdTable INSTANCE = new MainAutoIncIdTable();

    public final TableField<Record, Long> id = createPKField("id", SQLDataType.BIGINT.identity(true));
    public final TableField<Record, String> name = createField("name", SQLDataType.VARCHAR(50));
    public final TableField<Record, String> desc = createField("desc", SQLDataType.VARCHAR(50));
    public final TableField<Record, String> desc2 = createField("desc2", SQLDataType.VARCHAR(50));
    public final TableField<Record, Double> amount = createField("amount", SQLDataType.DOUBLE);
    public final TableField<Record, Double> amount2 = createField("amount2", SQLDataType.DOUBLE);
    public final TableField<Record, Integer> amount3 = createField("amount3", SQLDataType.INTEGER);

    private MainAutoIncIdTable() {
        super("main_auto_inc_id");
    }

    private MainAutoIncIdTable(final MainAutoIncIdTable aliased, final String alias) {
        super(aliased, alias);
    }

    @Override
    public MainAutoIncIdTable as(String alias) {
        return new MainAutoIncIdTable(this, alias);
    }
}
