package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.internal.audit.MainTable;
import com.kenshoo.pl.entity.internal.audit.formatters.InvalidAuditFieldValueFormatter;

@Audited
public class AuditedWithInvalidFieldLevelValueFormatterType extends AbstractType<AuditedWithInvalidFieldLevelValueFormatterType> {

    public static final AuditedWithInvalidFieldLevelValueFormatterType INSTANCE = new AuditedWithInvalidFieldLevelValueFormatterType();

    @Id
    public static final EntityField<AuditedWithInvalidFieldLevelValueFormatterType, Long> ID = INSTANCE.field(MainTable.INSTANCE.id);
    @Audited(valueFormatter = InvalidAuditFieldValueFormatter.class)
    public static final EntityField<AuditedWithInvalidFieldLevelValueFormatterType, String> NAME = INSTANCE.field(MainTable.INSTANCE.name);

    private AuditedWithInvalidFieldLevelValueFormatterType() {
        super("AuditedWithInvalidFieldValueFormatter");
    }
}
