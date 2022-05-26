package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.AbstractEntityType;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.SupportedChangeOperation;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.annotation.Required;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.internal.audit.ChildManualIdTable;

import static com.kenshoo.pl.entity.annotation.RequiredFieldType.RELATION;
import static com.kenshoo.pl.entity.audit.AuditTrigger.ALWAYS;

@Audited
public class AuditedChildManualIdType extends AbstractEntityType<AuditedChildManualIdType> {

    public static final AuditedChildManualIdType INSTANCE = new AuditedChildManualIdType();

    @Id
    public static final EntityField<AuditedChildManualIdType, Long> ID = INSTANCE.field(ChildManualIdTable.INSTANCE.id);
    @Audited(trigger = ALWAYS)
    @Required(RELATION)
    public static final EntityField<AuditedChildManualIdType, Long> PARENT_ID = INSTANCE.field(ChildManualIdTable.INSTANCE.parent_id);
    @Audited(trigger = ALWAYS)
    public static final EntityField<AuditedChildManualIdType, String> NAME = INSTANCE.field(ChildManualIdTable.INSTANCE.name);
    public static final EntityField<AuditedChildManualIdType, String> DESC = INSTANCE.field(ChildManualIdTable.INSTANCE.desc);

    @Override
    public DataTable getPrimaryTable() {
        return ChildManualIdTable.INSTANCE;
    }

    public SupportedChangeOperation getSupportedOperation() {
        return SupportedChangeOperation.CREATE_UPDATE_AND_DELETE;
    }

    private AuditedChildManualIdType() {
        super("AuditedChildManualId");
    }
}
