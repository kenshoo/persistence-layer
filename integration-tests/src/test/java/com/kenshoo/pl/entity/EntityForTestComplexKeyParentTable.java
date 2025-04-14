package com.kenshoo.pl.entity;

import com.kenshoo.jooq.AbstractDataTable;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;

public class EntityForTestComplexKeyParentTable extends AbstractDataTable<EntityForTestComplexKeyParentTable> {

    public static final EntityForTestComplexKeyParentTable INSTANCE = new EntityForTestComplexKeyParentTable("EntityForTestComplexKeyParent");

    final TableField<Record, Integer> id1 = createPKField("id1", SQLDataType.INTEGER.identity(true));
    final TableField<Record, Integer> id2 = createPKField("id2", SQLDataType.INTEGER.identity(true));
    final TableField<Record, String> field1 = createField("field1", SQLDataType.VARCHAR.length(50));

    public EntityForTestComplexKeyParentTable(String name) {
        super(name);
    }

    public EntityForTestComplexKeyParentTable(EntityForTestComplexKeyParentTable aliased, String alias) {
        super(aliased, alias);
    }

    @Override
    public EntityForTestComplexKeyParentTable as(String alias) {
        return new EntityForTestComplexKeyParentTable(this, alias);
    }
}
