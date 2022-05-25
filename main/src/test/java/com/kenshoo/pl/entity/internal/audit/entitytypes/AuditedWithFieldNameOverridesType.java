package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.internal.audit.MainAutoIncIdTable;

@Audited
public class AuditedWithFieldNameOverridesType extends AbstractAutoIncIdType<AuditedWithFieldNameOverridesType> {

    public static final AuditedWithFieldNameOverridesType INSTANCE = new AuditedWithFieldNameOverridesType();

    public static final String DESC_FIELD_NAME_OVERRIDE = "descOverride";

    @Id
    public static final EntityField<AuditedWithFieldNameOverridesType, Long> ID = INSTANCE.field(MainAutoIncIdTable.INSTANCE.id);
    public static final EntityField<AuditedWithFieldNameOverridesType, String> NAME = INSTANCE.field(MainAutoIncIdTable.INSTANCE.name);
    @Audited(name = DESC_FIELD_NAME_OVERRIDE)
    public static final EntityField<AuditedWithFieldNameOverridesType, String> DESC = INSTANCE.field(MainAutoIncIdTable.INSTANCE.desc);
    @Audited(name = "    ")
    public static final EntityField<AuditedWithFieldNameOverridesType, String> DESC2 = INSTANCE.field(MainAutoIncIdTable.INSTANCE.desc2);
    @Audited
    public static final EntityField<AuditedWithFieldNameOverridesType, Double> AMOUNT = INSTANCE.field(MainAutoIncIdTable.INSTANCE.amount);

    private AuditedWithFieldNameOverridesType() {
        super("AuditedWithFieldNameOverrides");
    }
}
