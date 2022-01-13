package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.jooq.AbstractDataTable;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;

import static org.jooq.impl.DSL.name;

public class MainTable extends AbstractDataTable<MainTable> {

    public static final MainTable INSTANCE = new MainTable("main");

    public final TableField<Record, Long> id = createPKField("id", SQLDataType.BIGINT.identity(true));
    public final TableField<Record, String> name = createField("name", SQLDataType.VARCHAR(50));
    public final TableField<Record, String> desc = createField("desc", SQLDataType.VARCHAR(50));
    public final TableField<Record, String> desc2 = createField("desc2", SQLDataType.VARCHAR(50));
    public final TableField<Record, Double> amount = createField("amount", SQLDataType.DOUBLE);
    public final TableField<Record, Double> amount2 = createField("amount2", SQLDataType.DOUBLE);
    public final TableField<Record, Integer> amount3 = createField(name("amount3"), SQLDataType.INTEGER);

    private MainTable(final String name) {
        super(name);
    }

    private MainTable(final MainTable aliased, final String alias) {
        super(aliased, alias);
    }

    @Override
    public MainTable as(String alias) {
        return new MainTable(this, alias);
    }
}
