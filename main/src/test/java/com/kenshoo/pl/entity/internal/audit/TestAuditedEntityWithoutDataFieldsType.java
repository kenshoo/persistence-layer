package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.Audited;
import com.kenshoo.pl.entity.annotation.Id;

@Audited
public class TestAuditedEntityWithoutDataFieldsType extends AbstractTestEntityType<TestAuditedEntityWithoutDataFieldsType> {

    public static final TestAuditedEntityWithoutDataFieldsType INSTANCE = new TestAuditedEntityWithoutDataFieldsType();

    @Id
    public static final EntityField<TestAuditedEntityWithoutDataFieldsType, Long> ID = INSTANCE.field(TestEntityTable.INSTANCE.id);

    private TestAuditedEntityWithoutDataFieldsType() {
        super("TestAuditedEntityWithoutDataFields");
    }
}
