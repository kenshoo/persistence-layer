package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.AbstractEntityType;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.SupportedChangeOperation;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.internal.audit.MainAutoIncIdTable;

import static com.kenshoo.pl.entity.audit.AuditTrigger.ALWAYS;

@Audited
public class AuditedWithInternalMandatoryType extends AbstractEntityType<AuditedWithInternalMandatoryType> {

    public static final AuditedWithInternalMandatoryType INSTANCE = new AuditedWithInternalMandatoryType();

    @Id
    public static final EntityField<AuditedWithInternalMandatoryType, Long> ID = INSTANCE.field(MainAutoIncIdTable.INSTANCE.id);
    @Audited(trigger = ALWAYS)
    public static final EntityField<AuditedWithInternalMandatoryType, String> NAME = INSTANCE.field(MainAutoIncIdTable.INSTANCE.name);
    public static final EntityField<AuditedWithInternalMandatoryType, String> DESC = INSTANCE.field(MainAutoIncIdTable.INSTANCE.desc);
    public static final EntityField<AuditedWithInternalMandatoryType, String> DESC2 = INSTANCE.field(MainAutoIncIdTable.INSTANCE.desc2);

    private AuditedWithInternalMandatoryType() {
        super("AuditedWithInternalMandatory");
    }

    @Override
    public SupportedChangeOperation getSupportedOperation() {
        return SupportedChangeOperation.CREATE_UPDATE_AND_DELETE;
    }

    @Override
    public DataTable getPrimaryTable() {
        return MainAutoIncIdTable.INSTANCE;
    }
}
