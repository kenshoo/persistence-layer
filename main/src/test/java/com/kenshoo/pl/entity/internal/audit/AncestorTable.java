package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.jooq.AbstractDataTable;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;

public class AncestorTable extends AbstractDataTable<AncestorTable> {

    public static final AncestorTable INSTANCE = new AncestorTable("ancestor");

    public final TableField<Record, Long> id = createPKField("id", SQLDataType.BIGINT.identity(true));
    public final TableField<Record, String> name = createField("name", SQLDataType.VARCHAR(50));
    public final TableField<Record, String> desc = createField("desc", SQLDataType.VARCHAR(50));

    private AncestorTable(final String name) {
        super(name);
    }

    private AncestorTable(final AncestorTable aliased, final String alias) {
        super(aliased, alias);
    }

    @Override
    public AncestorTable as(String alias) {
        return new AncestorTable(this, alias);
    }
}
