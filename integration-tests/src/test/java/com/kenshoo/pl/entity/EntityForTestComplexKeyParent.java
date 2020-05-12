package com.kenshoo.pl.entity;

import com.kenshoo.jooq.DataTable;

public class EntityForTestComplexKeyParent extends AbstractEntityType<EntityForTestComplexKeyParent> {

    public static final EntityForTestComplexKeyParent INSTANCE = new EntityForTestComplexKeyParent();

    public static final EntityField<EntityForTestComplexKeyParent, Integer> ID1 = INSTANCE.field(EntityForTestComplexKeyParentTable.INSTANCE.id1);
    public static final EntityField<EntityForTestComplexKeyParent, Integer> ID2 = INSTANCE.field(EntityForTestComplexKeyParentTable.INSTANCE.id2);
    public static final EntityField<EntityForTestComplexKeyParent, String> FIELD1 = INSTANCE.field(EntityForTestComplexKeyParentTable.INSTANCE.field1);

    private EntityForTestComplexKeyParent() {
        super("complex-key-parent");
    }

    @Override
    public DataTable getPrimaryTable() {
        return EntityForTestComplexKeyParentTable.INSTANCE;
    }
}
