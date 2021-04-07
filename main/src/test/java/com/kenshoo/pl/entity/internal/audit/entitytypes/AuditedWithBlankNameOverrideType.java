package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.internal.audit.MainTable;

@Audited(name = "   ")
public class AuditedWithBlankNameOverrideType extends AbstractType<AuditedWithBlankNameOverrideType> {

    public static final AuditedWithBlankNameOverrideType INSTANCE = new AuditedWithBlankNameOverrideType();

    @Id
    public static final EntityField<AuditedWithBlankNameOverrideType, Long> ID = INSTANCE.field(MainTable.INSTANCE.id);
    public static final EntityField<AuditedWithBlankNameOverrideType, String> DESC = INSTANCE.field(MainTable.INSTANCE.desc);

    private AuditedWithBlankNameOverrideType() {
        super("AuditedWithBlankTypeNameOverride");
    }
}
