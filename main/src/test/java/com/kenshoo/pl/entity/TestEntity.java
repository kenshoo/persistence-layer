package com.kenshoo.pl.entity;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.annotation.Id;

public class TestEntity extends AbstractEntityType<TestEntity> {

    public static final TestEntity INSTANCE = new TestEntity();

    @Id
    public static final EntityField<TestEntity, Integer> ID = INSTANCE.field(TestEntityTable.TABLE.id);
    public static final PrototypedEntityField<TestEntity, String> FIELD_1 = INSTANCE.prototypedField(TestDataFieldPrototype.FIELD_1, TestEntityTable.TABLE.field_1);
    public static final PrototypedEntityField<TestEntity, String> FIELD_2 = INSTANCE.prototypedField(TestDataFieldPrototype.FIELD_2, TestEntityTable.TABLE.field_2);
    public static final PrototypedEntityField<TestEntity, Integer> FIELD_3 = INSTANCE.prototypedField(TestDataFieldPrototype.FIELD_3, TestEntityTable.TABLE.field_3);
    public static final EntityField<TestEntity, String> SECONDARY_FIELD_1 = INSTANCE.field(SecondaryTable.TABLE.secondary_field_1);

    private TestEntity() {
        super("test");
    }

    public static class Key extends SingleUniqueKeyValue<TestEntity, Integer> {
        public static final SingleUniqueKey<TestEntity, Integer> DEFINITION = new SingleUniqueKey<TestEntity, Integer>(TestEntity.ID) {
            @Override
            protected SingleUniqueKeyValue<TestEntity, Integer> createValue(Integer value) {
                return new Key(value);
            }
        };

        public Key(int val) {
            super(DEFINITION, val);
        }
    }


    @Override
    public DataTable getPrimaryTable() {
        return TestEntityTable.TABLE;
    }
}
