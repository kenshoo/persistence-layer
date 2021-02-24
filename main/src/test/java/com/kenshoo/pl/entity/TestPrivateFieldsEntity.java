package com.kenshoo.pl.entity;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.annotation.Id;

/**
 * An entity simulating the Scala use-case where fields are private under the hood with synthetic getters
  */
public class TestPrivateFieldsEntity extends AbstractEntityType<TestPrivateFieldsEntity> {

    public static final TestPrivateFieldsEntity INSTANCE = new TestPrivateFieldsEntity();

    @Id
    private final EntityField<TestPrivateFieldsEntity, Integer> ID = field(TestEntityTable.TABLE.id);
    private final EntityField<TestPrivateFieldsEntity, String> FIELD_1 = field(TestEntityTable.TABLE.field_1);
    private final EntityField<TestPrivateFieldsEntity, String> SECONDARY_FIELD_1 = field(SecondaryTable.TABLE.secondary_field_1);

    private TestPrivateFieldsEntity() {
        super("test");
    }

    public EntityField<TestPrivateFieldsEntity, Integer> getID() {
        return ID;
    }

    public EntityField<TestPrivateFieldsEntity, String> getFIELD_1() {
        return FIELD_1;
    }

    public EntityField<TestPrivateFieldsEntity, String> getSECONDARY_FIELD_1() {
        return SECONDARY_FIELD_1;
    }

    @Override
    public DataTable getPrimaryTable() {
        return TestEntityTable.TABLE;
    }
}
