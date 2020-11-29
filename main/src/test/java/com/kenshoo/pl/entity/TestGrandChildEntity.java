package com.kenshoo.pl.entity;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.annotation.Required;
import com.kenshoo.pl.entity.annotation.RequiredFieldType;

public class TestGrandChildEntity extends AbstractEntityType<TestGrandChildEntity> {

    public static final TestGrandChildEntity INSTANCE = new TestGrandChildEntity();

    public static final EntityField<TestGrandChildEntity, Integer> ID = INSTANCE.field(TestGrandChildEntityTable.TABLE.id);
    @Required(RequiredFieldType.RELATION)
    public static final EntityField<TestGrandChildEntity, Integer> ORDINAL = INSTANCE.field(TestGrandChildEntityTable.TABLE.ordinal);
    public static final EntityField<TestGrandChildEntity, String>  GRAND_CHILD_FIELD_1 = INSTANCE.field(TestGrandChildEntityTable.TABLE.grand_child_field_1);
    public static final EntityField<TestGrandChildEntity, String>  GRAND_CHILD_FIELD_2 = INSTANCE.field(TestGrandChildEntityTable.TABLE.grand_child_field_2);
    public static final EntityField<TestGrandChildEntity, Integer> GRAND_CHILD_FIELD_3 = INSTANCE.field(TestGrandChildEntityTable.TABLE.grand_child_field_3);

    private TestGrandChildEntity() {
        super("testChildEntity");
    }

    public static class Key extends SingleUniqueKeyValue<TestGrandChildEntity, Integer> {
        public static final SingleUniqueKey<TestGrandChildEntity, Integer> DEFINITION = new SingleUniqueKey<TestGrandChildEntity, Integer>(TestGrandChildEntity.ID) {
            @Override
            protected SingleUniqueKeyValue<TestGrandChildEntity, Integer> createValue(Integer value) {
                return new Key(value);
            }
        };

        public Key(int val) {
            super(DEFINITION, val);
        }
    }


    @Override
    public DataTable getPrimaryTable() {
        return TestGrandChildEntityTable.TABLE;
    }
}
