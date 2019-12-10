package com.kenshoo.pl.entity;

import com.kenshoo.jooq.AbstractDataTable;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;

public class SecondaryTable extends AbstractDataTable<SecondaryTable> {

    public static final SecondaryTable TABLE = new SecondaryTable();

    private SecondaryTable() {
        super("secondaryTable");
    }

    public final TableField<Record, Integer> id = createPKField("id", SQLDataType.INTEGER);
    public final TableField<Record, Integer> entity_id = createFKField("entity_id", TestEntityTable.TABLE.id);
    public final TableField<Record, String> secondary_field_1 = createField("field_1", SQLDataType.VARCHAR.length(10));

    @Override
    public SecondaryTable as(String alias) {
        return null;
    }

}
