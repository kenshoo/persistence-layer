package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.AbstractEntityType;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.SupportedChangeOperation;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.internal.audit.MainWithoutIdTable;

import static com.kenshoo.pl.entity.audit.AuditTrigger.ALWAYS;

@Audited
public class AuditedWithoutIdType extends AbstractEntityType<AuditedWithoutIdType> {

    public static final AuditedWithoutIdType INSTANCE = new AuditedWithoutIdType();

    @Audited(trigger = ALWAYS)
    public static final EntityField<AuditedWithoutIdType, String> NAME = INSTANCE.field(MainWithoutIdTable.INSTANCE.name);
    public static final EntityField<AuditedWithoutIdType, String> DESC = INSTANCE.field(MainWithoutIdTable.INSTANCE.desc);

    public DataTable getPrimaryTable() {
        return MainWithoutIdTable.INSTANCE;
    }

    public SupportedChangeOperation getSupportedOperation() {
        return SupportedChangeOperation.CREATE_UPDATE_AND_DELETE;
    }
    private AuditedWithoutIdType() {
        super("AuditedWithoutId");
    }
}
