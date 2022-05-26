package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.AbstractEntityType;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.SupportedChangeOperation;
import com.kenshoo.pl.entity.annotation.Required;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.internal.audit.ChildWithoutIdOfParentWithoutIdTable;

import static com.kenshoo.pl.entity.annotation.RequiredFieldType.RELATION;
import static com.kenshoo.pl.entity.audit.AuditTrigger.ALWAYS;

@Audited
public class AuditedChildWithoutIdOfParentWithoutIdType extends AbstractEntityType<AuditedChildWithoutIdOfParentWithoutIdType> {

    public static final AuditedChildWithoutIdOfParentWithoutIdType INSTANCE = new AuditedChildWithoutIdOfParentWithoutIdType();

    @Audited(trigger = ALWAYS)
    @Required(RELATION)
    public static final EntityField<AuditedChildWithoutIdOfParentWithoutIdType, String> PARENT_NAME = INSTANCE.field(ChildWithoutIdOfParentWithoutIdTable.INSTANCE.parent_name);
    @Audited(trigger = ALWAYS)
    public static final EntityField<AuditedChildWithoutIdOfParentWithoutIdType, String> NAME = INSTANCE.field(ChildWithoutIdOfParentWithoutIdTable.INSTANCE.name);
    public static final EntityField<AuditedChildWithoutIdOfParentWithoutIdType, String> DESC = INSTANCE.field(ChildWithoutIdOfParentWithoutIdTable.INSTANCE.desc);

    @Override
    public DataTable getPrimaryTable() {
        return ChildWithoutIdOfParentWithoutIdTable.INSTANCE;
    }

    public SupportedChangeOperation getSupportedOperation() {
        return SupportedChangeOperation.CREATE_UPDATE_AND_DELETE;
    }

    private AuditedChildWithoutIdOfParentWithoutIdType() {
        super("AuditedChildWithoutIdOfParentWithoutId");
    }
}
