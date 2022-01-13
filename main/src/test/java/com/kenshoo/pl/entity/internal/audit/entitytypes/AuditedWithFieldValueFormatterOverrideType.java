package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.internal.audit.MainTable;
import com.kenshoo.pl.entity.internal.audit.formatters.CustomAuditFieldValueFormatter1;

@Audited
public class AuditedWithFieldValueFormatterOverrideType extends AbstractType<AuditedWithFieldValueFormatterOverrideType> {

    public static final AuditedWithFieldValueFormatterOverrideType INSTANCE = new AuditedWithFieldValueFormatterOverrideType();

    @Id
    public static final EntityField<AuditedWithFieldValueFormatterOverrideType, Long> ID = INSTANCE.field(MainTable.INSTANCE.id);
    @Audited(valueFormatter = CustomAuditFieldValueFormatter1.class)
    public static final EntityField<AuditedWithFieldValueFormatterOverrideType, String> NAME = INSTANCE.field(MainTable.INSTANCE.name);

    private AuditedWithFieldValueFormatterOverrideType() {
        super("AuditedWithFieldValueFormatterOverride");
    }
}
