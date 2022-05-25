package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.internal.audit.MainAutoIncIdTable;

@Audited(name = AuditedWithNameOverrideType.NAME_OVERRIDE)
public class AuditedWithNameOverrideType extends AbstractAutoIncIdType<AuditedWithNameOverrideType> {

    public static final String NAME_OVERRIDE = "Audited Override";

    public static final AuditedWithNameOverrideType INSTANCE = new AuditedWithNameOverrideType();

    @Id
    public static final EntityField<AuditedWithNameOverrideType, Long> ID = INSTANCE.field(MainAutoIncIdTable.INSTANCE.id);
    public static final EntityField<AuditedWithNameOverrideType, String> DESC = INSTANCE.field(MainAutoIncIdTable.INSTANCE.desc);

    private AuditedWithNameOverrideType() {
        super("AuditedWithNameOverride");
    }
}
