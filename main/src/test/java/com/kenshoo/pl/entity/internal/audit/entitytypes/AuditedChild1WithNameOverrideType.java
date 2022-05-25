package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.internal.audit.ChildAutoIncIdTable;

@Audited(name = AuditedChild1WithNameOverrideType.NAME_OVERRIDE)
public class AuditedChild1WithNameOverrideType extends AbstractAutoIncIdChildType<AuditedChild1WithNameOverrideType> {

    public static final String NAME_OVERRIDE = "AuditedChild1 Override";

    public static final AuditedChild1WithNameOverrideType INSTANCE = new AuditedChild1WithNameOverrideType();

    @Id
    public static final EntityField<AuditedChild1WithNameOverrideType, Long> ID = INSTANCE.field(ChildAutoIncIdTable.INSTANCE.id);
    public static final EntityField<AuditedChild1WithNameOverrideType, Long> PARENT_ID = INSTANCE.field(ChildAutoIncIdTable.INSTANCE.parent_id);
    public static final EntityField<AuditedChild1WithNameOverrideType, String> DESC = INSTANCE.field(ChildAutoIncIdTable.INSTANCE.desc);

    private AuditedChild1WithNameOverrideType() {
        super("AuditedChild1WithNameOverride");
    }
}
