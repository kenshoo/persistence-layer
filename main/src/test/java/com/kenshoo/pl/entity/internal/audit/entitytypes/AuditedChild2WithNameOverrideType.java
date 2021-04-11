package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.internal.audit.ChildTable;

@Audited(name = AuditedChild2WithNameOverrideType.NAME_OVERRIDE)
public class AuditedChild2WithNameOverrideType extends AbstractChildType<AuditedChild2WithNameOverrideType> {

    public static final String NAME_OVERRIDE = "AuditedChild2 Override";

    public static final AuditedChild2WithNameOverrideType INSTANCE = new AuditedChild2WithNameOverrideType();

    @Id
    public static final EntityField<AuditedChild2WithNameOverrideType, Long> ID = INSTANCE.field(ChildTable.INSTANCE.id);
    public static final EntityField<AuditedChild2WithNameOverrideType, Long> PARENT_ID = INSTANCE.field(ChildTable.INSTANCE.parent_id);
    public static final EntityField<AuditedChild2WithNameOverrideType, String> DESC = INSTANCE.field(ChildTable.INSTANCE.desc);

    private AuditedChild2WithNameOverrideType() {
        super("AuditedChild2WithNameOverride");
    }
}
