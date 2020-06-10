package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.AbstractEntityType;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.SupportedChangeOperation;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.internal.audit.MainTable;

import static com.kenshoo.pl.entity.audit.AuditTrigger.ALWAYS;

@Audited
public class AuditedWithSelfMandatoryType extends AbstractEntityType<AuditedWithSelfMandatoryType> {

    public static final AuditedWithSelfMandatoryType INSTANCE = new AuditedWithSelfMandatoryType();

    @Id
    public static final EntityField<AuditedWithSelfMandatoryType, Long> ID = INSTANCE.field(MainTable.INSTANCE.id);
    @Audited(trigger = ALWAYS)
    public static final EntityField<AuditedWithSelfMandatoryType, String> NAME = INSTANCE.field(MainTable.INSTANCE.name);
    public static final EntityField<AuditedWithSelfMandatoryType, String> DESC = INSTANCE.field(MainTable.INSTANCE.desc);
    public static final EntityField<AuditedWithSelfMandatoryType, String> DESC2 = INSTANCE.field(MainTable.INSTANCE.desc2);

    private AuditedWithSelfMandatoryType() {
        super("AuditedWithSelfMandatory");
    }

    @Override
    public SupportedChangeOperation getSupportedOperation() {
        return SupportedChangeOperation.CREATE_UPDATE_AND_DELETE;
    }

    @Override
    public DataTable getPrimaryTable() {
        return MainTable.INSTANCE;
    }
}
