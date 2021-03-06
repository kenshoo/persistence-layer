package com.kenshoo.pl.auto.inc;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.annotation.Id;

import static com.kenshoo.pl.entity.annotation.IdGeneration.RetrieveAutoGenerated;

public class TestEntity extends AbstractEntityType<TestEntity> {

    public static final TestEntity INSTANCE = new TestEntity();

    @Id(RetrieveAutoGenerated)
    public static final EntityField<TestEntity, Integer> ID = INSTANCE.field(TestEntityTable.INSTANCE.id);
    public static final EntityField<TestEntity, String> NAME = INSTANCE.field(TestEntityTable.INSTANCE.name);
    public static final EntityField<TestEntity, String> FIELD1 = INSTANCE.field(TestEntityTable.INSTANCE.field1);
    public static final EntityField<TestEntity, String> SECOND_NAME = INSTANCE.field(TestSecondaryEntityTable.INSTANCE.secondName);

    private TestEntity() {
        super("test");
    }

    @Override
    public DataTable getPrimaryTable() {
        return TestEntityTable.INSTANCE;
    }

    @Override
    public SupportedChangeOperation getSupportedOperation()  {
         return SupportedChangeOperation.CREATE_UPDATE_AND_DELETE;
    }

    public static class Key extends SingleUniqueKeyValue<TestEntity, Integer> {
        public static final SingleUniqueKey<TestEntity, Integer> DEFINITION = new SingleUniqueKey<TestEntity, Integer>(TestEntity.ID) {
            @Override
            protected Key createValue(Integer value) {
                return new Key(value);
            }
        };

        public Key(int id) {
            super(DEFINITION, id);
        }
    }
}
