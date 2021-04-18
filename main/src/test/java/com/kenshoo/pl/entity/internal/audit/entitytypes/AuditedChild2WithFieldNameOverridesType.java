package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.internal.audit.ChildTable;

@Audited
public class AuditedChild2WithFieldNameOverridesType extends AbstractChildType<AuditedChild2WithFieldNameOverridesType> {

    public static final AuditedChild2WithFieldNameOverridesType INSTANCE = new AuditedChild2WithFieldNameOverridesType();

    public static final String DESC_FIELD_NAME_OVERRIDE = "child2DescOverride";

    @Id
    public static final EntityField<AuditedChild2WithFieldNameOverridesType, Long> ID = INSTANCE.field(ChildTable.INSTANCE.id);
    public static final EntityField<AuditedChild2WithFieldNameOverridesType, Long> PARENT_ID = INSTANCE.field(ChildTable.INSTANCE.parent_id);
    @Audited(name = DESC_FIELD_NAME_OVERRIDE)
    public static final EntityField<AuditedChild2WithFieldNameOverridesType, String> DESC = INSTANCE.field(ChildTable.INSTANCE.desc);

    private AuditedChild2WithFieldNameOverridesType() {
        super("AuditedChild2WithFieldNameOverrides");
    }
}
