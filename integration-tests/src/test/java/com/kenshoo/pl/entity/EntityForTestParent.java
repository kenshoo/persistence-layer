package com.kenshoo.pl.entity;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.annotation.Required;

public class EntityForTestParent extends AbstractEntityType<EntityForTestParent> {

    public static final EntityForTestParent INSTANCE = new EntityForTestParent();

    public static final EntityField<EntityForTestParent, Integer> ID = INSTANCE.field(EntityForTestParentTable.INSTANCE.id);

    @Required
    public static final EntityField<EntityForTestParent, String> FIELD1 = INSTANCE.field(EntityForTestParentTable.INSTANCE.field1);

    private EntityForTestParent() {
        super("test_parent");
    }

    public static class Key extends SingleUniqueKeyValue<EntityForTestParent, Integer> {
        public static final SingleUniqueKey<EntityForTestParent, Integer> DEFINITION = new SingleUniqueKey<EntityForTestParent, Integer>(EntityForTestParent.ID) {
            @Override
            protected Key createValue(Integer value) {
                return new Key(value);
            }
        };

        public Key(int id) {
            super(DEFINITION, id);
        }
    }

    @Override
    public DataTable getPrimaryTable() {
        return EntityForTestParentTable.INSTANCE;
    }
}
