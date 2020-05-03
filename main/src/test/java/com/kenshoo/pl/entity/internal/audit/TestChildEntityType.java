package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.AbstractEntityType;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.SupportedChangeOperation;
import com.kenshoo.pl.entity.annotation.Id;

public class TestChildEntityType extends AbstractEntityType<TestChildEntityType> {

    public static final TestChildEntityType INSTANCE = new TestChildEntityType();

    @Id
    public static final EntityField<TestChildEntityType, Long> ID = INSTANCE.field(TestChildEntityTable.INSTANCE.id);
    public static final EntityField<TestChildEntityType, Long> PARENT_ID = INSTANCE.field(TestChildEntityTable.INSTANCE.parent_id);
    public static final EntityField<TestChildEntityType, String> NAME = INSTANCE.field(TestChildEntityTable.INSTANCE.name);
    public static final EntityField<TestChildEntityType, String> DESC = INSTANCE.field(TestChildEntityTable.INSTANCE.desc);

    @Override
    public DataTable getPrimaryTable() {
        return TestChildEntityTable.INSTANCE;
    }

    public SupportedChangeOperation getSupportedOperation() {
        return SupportedChangeOperation.CREATE_UPDATE_AND_DELETE;
    }

    private TestChildEntityType() {
        super("TestChildEntity");
    }
}
