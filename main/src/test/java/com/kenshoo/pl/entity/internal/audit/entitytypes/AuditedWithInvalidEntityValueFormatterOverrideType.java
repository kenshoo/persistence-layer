package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.internal.audit.MainTable;
import com.kenshoo.pl.entity.internal.audit.formatters.InvalidAuditFieldValueFormatter;

@Audited(valueFormatter = InvalidAuditFieldValueFormatter.class)
public class AuditedWithInvalidEntityValueFormatterOverrideType extends AbstractType<AuditedWithInvalidEntityValueFormatterOverrideType> {

    public static final AuditedWithInvalidEntityValueFormatterOverrideType INSTANCE = new AuditedWithInvalidEntityValueFormatterOverrideType();

    @Id
    public static final EntityField<AuditedWithInvalidEntityValueFormatterOverrideType, Long> ID = INSTANCE.field(MainTable.INSTANCE.id);
    public static final EntityField<AuditedWithInvalidEntityValueFormatterOverrideType, String> NAME = INSTANCE.field(MainTable.INSTANCE.name);

    private AuditedWithInvalidEntityValueFormatterOverrideType() {
        super("AuditedWithInvalidEntityValueFormatterOverride");
    }
}
