package com.kenshoo.pl.entity;

import com.kenshoo.jooq.AbstractDataTable;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;

public class EntityForTestParentTable extends AbstractDataTable<EntityForTestParentTable> {

    public static final EntityForTestParentTable INSTANCE = new EntityForTestParentTable("EntityForTestParent");

    final TableField<Record, Integer> id = createPKField("id", SQLDataType.INTEGER.identity(true));
    final TableField<Record, String> field1 = createField("field1", SQLDataType.VARCHAR.length(50));

    public EntityForTestParentTable(String name) {
        super(name);
    }

    public EntityForTestParentTable(EntityForTestParentTable aliased, String alias) {
        super(aliased, alias);
    }

    @Override
    public EntityForTestParentTable as(String alias) {
        return new EntityForTestParentTable(this, alias);
    }
}
