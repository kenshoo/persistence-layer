package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.internal.audit.ChildAutoIncIdTable;

@Audited
public class AuditedChild1WithFieldNameOverridesType extends AbstractAutoIncIdChildType<AuditedChild1WithFieldNameOverridesType> {

    public static final AuditedChild1WithFieldNameOverridesType INSTANCE = new AuditedChild1WithFieldNameOverridesType();

    public static final String DESC_FIELD_NAME_OVERRIDE = "child1DescOverride";

    @Id
    public static final EntityField<AuditedChild1WithFieldNameOverridesType, Long> ID = INSTANCE.field(ChildAutoIncIdTable.INSTANCE.id);
    public static final EntityField<AuditedChild1WithFieldNameOverridesType, Long> PARENT_ID = INSTANCE.field(ChildAutoIncIdTable.INSTANCE.parent_id);
    @Audited(name = DESC_FIELD_NAME_OVERRIDE)
    public static final EntityField<AuditedChild1WithFieldNameOverridesType, String> DESC = INSTANCE.field(ChildAutoIncIdTable.INSTANCE.desc);

    private AuditedChild1WithFieldNameOverridesType() {
        super("AuditedChild1WithNameOverride");
    }
}
