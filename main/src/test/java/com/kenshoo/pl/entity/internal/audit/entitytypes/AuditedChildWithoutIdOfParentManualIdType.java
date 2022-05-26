package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.AbstractEntityType;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.SupportedChangeOperation;
import com.kenshoo.pl.entity.annotation.Required;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.internal.audit.ChildWithoutIdOfParentManualIdTable;

import static com.kenshoo.pl.entity.annotation.RequiredFieldType.RELATION;
import static com.kenshoo.pl.entity.audit.AuditTrigger.ALWAYS;

@Audited
public class AuditedChildWithoutIdOfParentManualIdType extends AbstractEntityType<AuditedChildWithoutIdOfParentManualIdType> {

    public static final AuditedChildWithoutIdOfParentManualIdType INSTANCE = new AuditedChildWithoutIdOfParentManualIdType();

    @Audited(trigger = ALWAYS)
    @Required(RELATION)
    public static final EntityField<AuditedChildWithoutIdOfParentManualIdType, Long> PARENT_ID = INSTANCE.field(ChildWithoutIdOfParentManualIdTable.INSTANCE.parent_id);
    @Audited(trigger = ALWAYS)
    public static final EntityField<AuditedChildWithoutIdOfParentManualIdType, String> NAME = INSTANCE.field(ChildWithoutIdOfParentManualIdTable.INSTANCE.name);
    public static final EntityField<AuditedChildWithoutIdOfParentManualIdType, String> DESC = INSTANCE.field(ChildWithoutIdOfParentManualIdTable.INSTANCE.desc);

    @Override
    public DataTable getPrimaryTable() {
        return ChildWithoutIdOfParentManualIdTable.INSTANCE;
    }

    public SupportedChangeOperation getSupportedOperation() {
        return SupportedChangeOperation.CREATE_UPDATE_AND_DELETE;
    }

    private AuditedChildWithoutIdOfParentManualIdType() {
        super("AuditedChildWithoutIdOfParentManualId");
    }
}
