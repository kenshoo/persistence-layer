package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.AbstractEntityType;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.SupportedChangeOperation;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.internal.audit.MainManualIdTable;

@Audited
public class AuditedManualIdType extends AbstractEntityType<AuditedManualIdType> {

    public static final AuditedManualIdType INSTANCE = new AuditedManualIdType();

    @Id
    public static final EntityField<AuditedManualIdType, Long> ID = INSTANCE.field(MainManualIdTable.INSTANCE.id);
    public static final EntityField<AuditedManualIdType, String> NAME = INSTANCE.field(MainManualIdTable.INSTANCE.name);
    public static final EntityField<AuditedManualIdType, String> DESC = INSTANCE.field(MainManualIdTable.INSTANCE.desc);

    public DataTable getPrimaryTable() {
        return MainManualIdTable.INSTANCE;
    }

    public SupportedChangeOperation getSupportedOperation() {
        return SupportedChangeOperation.CREATE_UPDATE_AND_DELETE;
    }
    private AuditedManualIdType() {
        super("AuditedManualId");
    }
}
