package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.jooq.AbstractDataTable;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;

import static org.jooq.impl.DSL.name;

public class MainWithoutIdTable extends AbstractDataTable<MainWithoutIdTable> {

    public static final MainWithoutIdTable INSTANCE = new MainWithoutIdTable();

    public final TableField<Record, String> name = createPKField("name", SQLDataType.VARCHAR(50));
    public final TableField<Record, String> desc = createField(name("desc"), SQLDataType.VARCHAR(50));
    public final TableField<Record, String> desc2 = createField(name("desc2"), SQLDataType.VARCHAR(50));

    private MainWithoutIdTable() {
        super("main_without_id");
    }

    private MainWithoutIdTable(final MainWithoutIdTable aliased, final String alias) {
        super(aliased, alias);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public MainWithoutIdTable as(String alias) {
        return new MainWithoutIdTable(this, alias);
    }
}
