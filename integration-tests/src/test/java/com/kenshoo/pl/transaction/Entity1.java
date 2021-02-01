package com.kenshoo.pl.transaction;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.one2many.relatedByPK.ParentEntity;
import com.kenshoo.pl.one2many.relatedByPK.ParentTable;


public class Entity1 extends AbstractEntityType<Entity1> {

    public static final Entity1 INSTANCE = new Entity1();

    @Id
    public static final EntityField<Entity1, Integer> ID = INSTANCE.field(Table1.INSTANCE.id);

    public static final EntityField<Entity1, String> NAME = INSTANCE.field(Table1.INSTANCE.name);

    private Entity1() {
        super("entity1");
    }

    @Override
    public DataTable getPrimaryTable() {
        return Table1.INSTANCE;
    }

    @Override
    public SupportedChangeOperation getSupportedOperation() {
        return SupportedChangeOperation.CREATE_UPDATE_AND_DELETE;
    }

    public static class Key extends SingleUniqueKey<Entity1, Integer> {
        public Key() {
            super(Entity1.ID);
        }
    }

}
