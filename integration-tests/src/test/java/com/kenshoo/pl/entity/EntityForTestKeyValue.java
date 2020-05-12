package com.kenshoo.pl.entity;

public class EntityForTestKeyValue extends PairUniqueKeyValue<EntityForTest, TestEnum, Integer> {

    private static final PairUniqueKey<EntityForTest, TestEnum, Integer> DEFINITION = new PairUniqueKey<EntityForTest, TestEnum, Integer>(EntityForTest.FIELD1, EntityForTest.FIELD2) {
        @Override
        protected EntityForTestKeyValue createValue(TestEnum f1, Integer f2) {
            return new EntityForTestKeyValue(f1, f2);
        }
    };

    public EntityForTestKeyValue(TestEnum f1, int f2) {
        super(DEFINITION, f1, f2);
    }
}
