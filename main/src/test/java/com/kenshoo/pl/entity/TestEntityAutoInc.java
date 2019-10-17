package com.kenshoo.pl.entity;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.annotation.Id;

public class TestEntityAutoInc extends AbstractEntityType<TestEntityAutoInc> {

    public static final TestEntityAutoInc INSTANCE = new TestEntityAutoInc();

    @Id
    public static final EntityField<TestEntityAutoInc, Integer> ID = INSTANCE.field(TestEntityAutoIncTable.TABLE.id);
    public static final PrototypedEntityField<TestEntityAutoInc, String> FIELD_1 = INSTANCE.prototypedField(TestDataFieldPrototype.FIELD_1, TestEntityAutoIncTable.TABLE.field_1);

    private TestEntityAutoInc() {
        super("test");
    }

    public static class Key extends SingleUniqueKeyValue<TestEntityAutoInc, Integer> {
        public static final SingleUniqueKey<TestEntityAutoInc, Integer> DEFINITION = new SingleUniqueKey<TestEntityAutoInc, Integer>(TestEntityAutoInc.ID) {
            @Override
            protected SingleUniqueKeyValue<TestEntityAutoInc, Integer> createValue(Integer value) {
                return new Key(value);
            }
        };

        public Key(int val) {
            super(DEFINITION, val);
        }
    }


    @Override
    public DataTable getPrimaryTable() {
        return TestEntityAutoIncTable.TABLE;
    }
}
