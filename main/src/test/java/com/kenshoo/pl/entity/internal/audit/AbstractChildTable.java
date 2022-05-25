package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.jooq.AbstractDataTable;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;

public class AbstractChildTable extends AbstractDataTable<AbstractChildTable> {

    public final TableField<Record, String> name = createField("name", SQLDataType.VARCHAR(50));
    public final TableField<Record, String> desc = createField("desc", SQLDataType.VARCHAR(50));

    protected AbstractChildTable() {
        super("child");
    }

    protected AbstractChildTable(final AbstractChildTable aliased, final String alias) {
        super(aliased, alias);
    }

    @Override
    public AbstractChildTable as(String alias) {
        return new AbstractChildTable(this, alias);
    }
}
