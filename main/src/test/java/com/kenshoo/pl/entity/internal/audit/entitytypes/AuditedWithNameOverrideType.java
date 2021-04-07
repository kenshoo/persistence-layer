package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.internal.audit.MainTable;

@Audited(name = AuditedWithNameOverrideType.NAME_OVERRIDE)
public class AuditedWithNameOverrideType extends AbstractType<AuditedWithNameOverrideType> {

    public static final String NAME_OVERRIDE = "Audited Override";

    public static final AuditedWithNameOverrideType INSTANCE = new AuditedWithNameOverrideType();

    @Id
    public static final EntityField<AuditedWithNameOverrideType, Long> ID = INSTANCE.field(MainTable.INSTANCE.id);
    public static final EntityField<AuditedWithNameOverrideType, String> DESC = INSTANCE.field(MainTable.INSTANCE.desc);

    private AuditedWithNameOverrideType() {
        super("AuditedWithNameOverride");
    }
}
