package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.jooq.AbstractDataTable;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;

public class AbstractMainTable extends AbstractDataTable<AbstractMainTable> {

    public final TableField<Record, String> name = createField("name", SQLDataType.VARCHAR(50));
    public final TableField<Record, String> desc = createField("desc", SQLDataType.VARCHAR(50));
    public final TableField<Record, String> desc2 = createField("desc2", SQLDataType.VARCHAR(50));
    public final TableField<Record, Double> amount = createField("amount", SQLDataType.DOUBLE);
    public final TableField<Record, Double> amount2 = createField("amount2", SQLDataType.DOUBLE);
    public final TableField<Record, Integer> amount3 = createField("amount3", SQLDataType.INTEGER);

    protected AbstractMainTable() {
        super("main");
    }

    protected AbstractMainTable(final AbstractMainTable aliased, final String alias) {
        super(aliased, alias);
    }

    @Override
    public AbstractMainTable as(String alias) {
        return new AbstractMainTable(this, alias);
    }
}
