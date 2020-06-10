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
public class AuditedWithSelfMandatoryOnlyType extends AbstractEntityType<AuditedWithSelfMandatoryOnlyType> {

    public static final AuditedWithSelfMandatoryOnlyType INSTANCE = new AuditedWithSelfMandatoryOnlyType();

    @Id
    public static final EntityField<AuditedWithSelfMandatoryOnlyType, Long> ID = INSTANCE.field(MainTable.INSTANCE.id);
    @Audited(trigger = ALWAYS)
    public static final EntityField<AuditedWithSelfMandatoryOnlyType, String> NAME = INSTANCE.field(MainTable.INSTANCE.name);

    private AuditedWithSelfMandatoryOnlyType() {
        super("AuditedWithSelfMandatoryOnly");
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
