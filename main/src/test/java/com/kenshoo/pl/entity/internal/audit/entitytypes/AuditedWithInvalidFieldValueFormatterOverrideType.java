package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.internal.audit.MainTable;
import com.kenshoo.pl.entity.internal.audit.formatters.InvalidAuditFieldValueFormatter;

@Audited
public class AuditedWithInvalidFieldValueFormatterOverrideType extends AbstractType<AuditedWithInvalidFieldValueFormatterOverrideType> {

    public static final AuditedWithInvalidFieldValueFormatterOverrideType INSTANCE = new AuditedWithInvalidFieldValueFormatterOverrideType();

    @Id
    public static final EntityField<AuditedWithInvalidFieldValueFormatterOverrideType, Long> ID = INSTANCE.field(MainTable.INSTANCE.id);
    @Audited(valueFormatter = InvalidAuditFieldValueFormatter.class)
    public static final EntityField<AuditedWithInvalidFieldValueFormatterOverrideType, String> NAME = INSTANCE.field(MainTable.INSTANCE.name);

    private AuditedWithInvalidFieldValueFormatterOverrideType() {
        super("AuditedWithInvalidFieldValueFormatterOverride");
    }
}
