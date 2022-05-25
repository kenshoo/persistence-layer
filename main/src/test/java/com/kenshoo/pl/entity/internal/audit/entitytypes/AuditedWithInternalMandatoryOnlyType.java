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
public class AuditedWithInternalMandatoryOnlyType extends AbstractEntityType<AuditedWithInternalMandatoryOnlyType> {

    public static final AuditedWithInternalMandatoryOnlyType INSTANCE = new AuditedWithInternalMandatoryOnlyType();

    @Id
    public static final EntityField<AuditedWithInternalMandatoryOnlyType, Long> ID = INSTANCE.field(MainAutoIncIdTable.INSTANCE.id);
    @Audited(trigger = ALWAYS)
    public static final EntityField<AuditedWithInternalMandatoryOnlyType, String> NAME = INSTANCE.field(MainAutoIncIdTable.INSTANCE.name);

    private AuditedWithInternalMandatoryOnlyType() {
        super("AuditedWithInternalMandatoryOnly");
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
