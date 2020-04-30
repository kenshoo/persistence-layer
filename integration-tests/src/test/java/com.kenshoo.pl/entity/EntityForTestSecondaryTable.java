package com.kenshoo.pl.entity;

import com.kenshoo.jooq.AbstractDataTable;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;

/**
 * Created by yuvalr on 10/1/15.
 */
public class EntityForTestSecondaryTable extends AbstractDataTable<EntityForTestSecondaryTable> {

    public static final EntityForTestSecondaryTable INSTANCE = new EntityForTestSecondaryTable("SECONDARY_DUMMY");

    final TableField<Record, Integer> id = createPKField("id", SQLDataType.INTEGER);
    final TableField<Record, Integer> entityId = createFKField("entity_id", EntityForTestTable.INSTANCE.id);
    final TableField<Record, String> url = createField("entity_url", SQLDataType.VARCHAR.length(100).nullable(false));
    final TableField<Record, String> url_param = createField("url_param", SQLDataType.VARCHAR.length(100));

    public EntityForTestSecondaryTable(String name) {
        super(name);
    }

    public EntityForTestSecondaryTable(EntityForTestSecondaryTable aliased, String alias) {
        super(aliased, alias);
    }

    @Override
    public EntityForTestSecondaryTable as(String alias) {
        return new EntityForTestSecondaryTable(this, alias);
    }
}
