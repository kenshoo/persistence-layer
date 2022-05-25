package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.internal.audit.MainAutoIncIdTable;

@Audited(name = "   ")
public class AuditedWithBlankNameOverrideType extends AbstractAutoIncIdType<AuditedWithBlankNameOverrideType> {

    public static final AuditedWithBlankNameOverrideType INSTANCE = new AuditedWithBlankNameOverrideType();

    @Id
    public static final EntityField<AuditedWithBlankNameOverrideType, Long> ID = INSTANCE.field(MainAutoIncIdTable.INSTANCE.id);
    public static final EntityField<AuditedWithBlankNameOverrideType, String> DESC = INSTANCE.field(MainAutoIncIdTable.INSTANCE.desc);

    private AuditedWithBlankNameOverrideType() {
        super("AuditedWithBlankTypeNameOverride");
    }
}
