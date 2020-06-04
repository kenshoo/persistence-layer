package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.AbstractEntityType;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.internal.audit.MainWithAncestorTable;
import com.kenshoo.pl.entity.internal.audit.ancestorfieldsproviders.InvalidAuditExtensions;

@Audited(extensions = InvalidAuditExtensions.class)
public class AuditedWithInvalidMandatoryType extends AbstractEntityType<AuditedWithInvalidMandatoryType> {

    public static final AuditedWithInvalidMandatoryType INSTANCE = new AuditedWithInvalidMandatoryType();

    @Id
    public static final EntityField<AuditedWithInvalidMandatoryType, Long> ID = INSTANCE.field(MainWithAncestorTable.INSTANCE.id);
    public static final EntityField<AuditedWithInvalidMandatoryType, String> NAME = INSTANCE.field(MainWithAncestorTable.INSTANCE.name);
    public static final EntityField<AuditedWithInvalidMandatoryType, String> DESC = INSTANCE.field(MainWithAncestorTable.INSTANCE.desc);
    public static final EntityField<AuditedWithInvalidMandatoryType, String> DESC2 = INSTANCE.field(MainWithAncestorTable.INSTANCE.desc2);

    private AuditedWithInvalidMandatoryType() {
        super("AuditedWithInvalidMandatory");
    }

    @Override
    public DataTable getPrimaryTable() {
        return MainWithAncestorTable.INSTANCE;
    }
}
