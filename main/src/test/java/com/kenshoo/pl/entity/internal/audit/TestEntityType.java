package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.Id;

public class TestEntityType extends AbstractTestEntityType<TestEntityType> {

    public static final TestEntityType INSTANCE = new TestEntityType();

    @Id
    public static final EntityField<TestEntityType, Long> ID = INSTANCE.field(TestEntityTable.INSTANCE.id);
    public static final EntityField<TestEntityType, String> NAME = INSTANCE.field(TestEntityTable.INSTANCE.name);
    public static final EntityField<TestEntityType, String> DESC = INSTANCE.field(TestEntityTable.INSTANCE.desc);
    public static final EntityField<TestEntityType, String> DESC2 = INSTANCE.field(TestEntityTable.INSTANCE.desc2);

    private TestEntityType() {
        super("TestEntity");
    }
}
