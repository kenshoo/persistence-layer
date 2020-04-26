package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.Audited;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.annotation.NotAudited;

@Audited
public class TestAuditedEntityWithNotAuditedFieldsType extends AbstractTestEntityType<TestAuditedEntityWithNotAuditedFieldsType> {

    public static final TestAuditedEntityWithNotAuditedFieldsType INSTANCE = new TestAuditedEntityWithNotAuditedFieldsType();

    @Id
    public static final EntityField<TestAuditedEntityWithNotAuditedFieldsType, Long> ID = INSTANCE.field(TestEntityTable.INSTANCE.id);
    public static final EntityField<TestAuditedEntityWithNotAuditedFieldsType, String> NAME = INSTANCE.field(TestEntityTable.INSTANCE.name);
    @NotAudited
    public static final EntityField<TestAuditedEntityWithNotAuditedFieldsType, String> DESC = INSTANCE.field(TestEntityTable.INSTANCE.desc);
    @NotAudited
    public static final EntityField<TestAuditedEntityWithNotAuditedFieldsType, String> DESC2 = INSTANCE.field(TestEntityTable.INSTANCE.desc2);

    private TestAuditedEntityWithNotAuditedFieldsType() {
        super("TestAuditedEntityWithNotAuditedFields");
    }
}
