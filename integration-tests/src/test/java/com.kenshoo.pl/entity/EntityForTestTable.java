package com.kenshoo.pl.entity;

import com.kenshoo.jooq.AbstractDataTable;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;

import java.sql.Timestamp;

public class EntityForTestTable extends AbstractDataTable<EntityForTestTable> {

    public static final EntityForTestTable INSTANCE = new EntityForTestTable("EntityForTest");

    final TableField<Record, Integer> id = createPKField("id", SQLDataType.INTEGER);
    final TableField<Record, String> field1 = createField("field1", SQLDataType.VARCHAR.length(50));
    final TableField<Record, Integer> field2 = createField("field2", SQLDataType.INTEGER);
    final TableField<Record, Timestamp> creationDate = createField("creationDate", SQLDataType.TIMESTAMP);
    final TableField<Record, String> complexFieldKey = createField("complexFieldKey", SQLDataType.VARCHAR.length(50));
    final TableField<Record, String> complexFieldValue = createField("complexFieldValue", SQLDataType.VARCHAR.length(50));

    final TableField<Record, Integer> parent_id = createFKField("parent_id", EntityForTestParentTable.INSTANCE.id);
    final TableField<Record, Integer> parent_id1 = createFKField("parent_id1", EntityForTestComplexKeyParentTable.INSTANCE.id1);
    final TableField<Record, Integer> parent_id2 = createFKField("parent_id2", EntityForTestComplexKeyParentTable.INSTANCE.id2);
    final TableField<Record, Integer> ignorableField = createField("ignorableField", SQLDataType.INTEGER);

    public EntityForTestTable(String name) {
        super(name);
    }

    public EntityForTestTable(EntityForTestTable aliased, String alias) {
        super(aliased, alias);
    }

    @Override
    public EntityForTestTable as(String alias) {
        return new EntityForTestTable(this, alias);
    }
}
