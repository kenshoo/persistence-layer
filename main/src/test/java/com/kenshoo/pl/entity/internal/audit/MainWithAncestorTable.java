package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.jooq.AbstractDataTable;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;

public class MainWithAncestorTable extends AbstractDataTable<MainWithAncestorTable> {

    public static final MainWithAncestorTable INSTANCE = new MainWithAncestorTable("main_with_ancestor");

    public final TableField<Record, Long> id = createPKField("id", SQLDataType.BIGINT.identity(true));
    public final TableField<Record, Long> ancestor_id = createFKField("ancestor_id", AncestorTable.INSTANCE.id);
    public final TableField<Record, String> name = createField("name", SQLDataType.VARCHAR(50));
    public final TableField<Record, String> desc = createField("desc", SQLDataType.VARCHAR(50));
    public final TableField<Record, String> desc2 = createField("desc2", SQLDataType.VARCHAR(50));

    private MainWithAncestorTable(final String name) {
        super(name);
    }

    private MainWithAncestorTable(final MainWithAncestorTable aliased, final String alias) {
        super(aliased, alias);
    }

    @Override
    public MainWithAncestorTable as(String alias) {
        return new MainWithAncestorTable(this, alias);
    }
}
