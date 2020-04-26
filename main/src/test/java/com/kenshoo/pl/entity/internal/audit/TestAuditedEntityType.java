package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.Audited;
import com.kenshoo.pl.entity.annotation.Id;

@Audited
public class TestAuditedEntityType extends AbstractTestEntityType<TestAuditedEntityType> {

    public static final TestAuditedEntityType INSTANCE = new TestAuditedEntityType();

    @Id
    public static final EntityField<TestAuditedEntityType, Long> ID = INSTANCE.field(TestEntityTable.INSTANCE.id);
    public static final EntityField<TestAuditedEntityType, String> NAME = INSTANCE.field(TestEntityTable.INSTANCE.name);
    public static final EntityField<TestAuditedEntityType, String> DESC = INSTANCE.field(TestEntityTable.INSTANCE.desc);
    public static final EntityField<TestAuditedEntityType, String> DESC2 = INSTANCE.field(TestEntityTable.INSTANCE.desc2);

    private TestAuditedEntityType() {
        super("TestAuditedEntity");
    }
}
