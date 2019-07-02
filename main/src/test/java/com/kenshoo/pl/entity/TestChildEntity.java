package com.kenshoo.pl.entity;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.annotation.Required;
import static com.kenshoo.pl.entity.annotation.RequiredFieldType.RELATION;

public class TestChildEntity extends AbstractEntityType<TestChildEntity> {

    public static final TestChildEntity INSTANCE = new TestChildEntity();

    public static final EntityField<TestChildEntity, Integer> ORDINAL = INSTANCE.field(TestChildEntityTable.TABLE.ordinal);
    @Required(RELATION)
    public static final EntityField<TestChildEntity, Integer> PARENT_ID = INSTANCE.field(TestChildEntityTable.TABLE.parent_id);
    public static final EntityField<TestChildEntity, String>  CHILD_FIELD_1 = INSTANCE.field(TestChildEntityTable.TABLE.child_field_1);
    public static final EntityField<TestChildEntity, String>  CHILD_FIELD_2 = INSTANCE.field(TestChildEntityTable.TABLE.child_field_2);
    public static final EntityField<TestChildEntity, Integer> CHILD_FIELD_3 = INSTANCE.field(TestChildEntityTable.TABLE.child_field_3);

    private TestChildEntity() {
        super("testChildEntity");
    }

    public static class Ordinal extends SingleUniqueKeyValue<TestChildEntity, Integer> {
        public static final SingleUniqueKey<TestChildEntity, Integer> DEFINITION = new SingleUniqueKey<TestChildEntity, Integer>(TestChildEntity.ORDINAL) {
            @Override
            protected SingleUniqueKeyValue<TestChildEntity, Integer> createValue(Integer value) {
                return new Ordinal(value);
            }
        };

        public Ordinal(int val) {
            super(DEFINITION, val);
        }
    }


    @Override
    public DataTable getPrimaryTable() {
        return TestChildEntityTable.TABLE;
    }
}
