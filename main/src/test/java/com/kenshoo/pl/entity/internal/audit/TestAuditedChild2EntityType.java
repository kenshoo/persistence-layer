package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.AbstractEntityType;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.SupportedChangeOperation;
import com.kenshoo.pl.entity.annotation.Audited;
import com.kenshoo.pl.entity.annotation.Id;

@Audited
public class TestAuditedChild2EntityType extends AbstractEntityType<TestAuditedChild2EntityType> {

    public static final TestAuditedChild2EntityType INSTANCE = new TestAuditedChild2EntityType();

    @Id
    public static final EntityField<TestAuditedChild2EntityType, Long> ID = INSTANCE.field(TestChildEntityTable.INSTANCE.id);
    public static final EntityField<TestAuditedChild2EntityType, Long> PARENT_ID = INSTANCE.field(TestChildEntityTable.INSTANCE.parent_id);
    public static final EntityField<TestAuditedChild2EntityType, String> NAME = INSTANCE.field(TestChildEntityTable.INSTANCE.name);
    public static final EntityField<TestAuditedChild2EntityType, String> DESC = INSTANCE.field(TestChildEntityTable.INSTANCE.desc);

    @Override
    public DataTable getPrimaryTable() {
        return TestChildEntityTable.INSTANCE;
    }

    public SupportedChangeOperation getSupportedOperation() {
        return SupportedChangeOperation.CREATE_UPDATE_AND_DELETE;
    }

    private TestAuditedChild2EntityType() {
        super("TestAuditedChild2Entity");
    }
}
