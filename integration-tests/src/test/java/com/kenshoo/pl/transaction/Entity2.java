package com.kenshoo.pl.transaction;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.one2many.relatedByPK.ParentTable;


public class Entity2 extends AbstractEntityType<Entity2> {

    public static final Entity2 INSTANCE = new Entity2();

    @Id
    public static final EntityField<Entity2, Integer> ID = INSTANCE.field(Table2.INSTANCE.id);

    public static final EntityField<Entity2, String> NAME = INSTANCE.field(Table2.INSTANCE.name);

    private Entity2() {
        super("entity2");
    }

    @Override
    public DataTable getPrimaryTable() {
        return Table2.INSTANCE;
    }

    @Override
    public SupportedChangeOperation getSupportedOperation() {
        return SupportedChangeOperation.CREATE_UPDATE_AND_DELETE;
    }

    public static class Key extends SingleUniqueKey<Entity2, Integer> {
        public Key() {
            super(Entity2.ID);
        }
    }
}
