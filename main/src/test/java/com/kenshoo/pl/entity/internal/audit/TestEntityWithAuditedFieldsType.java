package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.Audited;
import com.kenshoo.pl.entity.annotation.Id;

public class TestEntityWithAuditedFieldsType extends AbstractTestEntityType<TestEntityWithAuditedFieldsType> {

    public static final TestEntityWithAuditedFieldsType INSTANCE = new TestEntityWithAuditedFieldsType();

    @Id
    public static final EntityField<TestEntityWithAuditedFieldsType, Long> ID = INSTANCE.field(TestEntityTable.INSTANCE.id);
    @Audited
    public static final EntityField<TestEntityWithAuditedFieldsType, String> NAME = INSTANCE.field(TestEntityTable.INSTANCE.name);
    @Audited
    public static final EntityField<TestEntityWithAuditedFieldsType, String> DESC = INSTANCE.field(TestEntityTable.INSTANCE.desc);
    public static final EntityField<TestEntityWithAuditedFieldsType, String> DESC2 = INSTANCE.field(TestEntityTable.INSTANCE.desc2);

    private TestEntityWithAuditedFieldsType() {
        super("TestEntityWithAuditedFields");
    }
}
