package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.internal.audit.MainAutoIncIdTable;
import com.kenshoo.pl.entity.internal.audit.formatters.InvalidAuditFieldValueFormatter;

@Audited
public class AuditedWithInvalidFieldLevelValueFormatterType extends AbstractAutoIncIdType<AuditedWithInvalidFieldLevelValueFormatterType> {

    public static final AuditedWithInvalidFieldLevelValueFormatterType INSTANCE = new AuditedWithInvalidFieldLevelValueFormatterType();

    @Id
    public static final EntityField<AuditedWithInvalidFieldLevelValueFormatterType, Long> ID = INSTANCE.field(MainAutoIncIdTable.INSTANCE.id);
    @Audited(valueFormatter = InvalidAuditFieldValueFormatter.class)
    public static final EntityField<AuditedWithInvalidFieldLevelValueFormatterType, String> NAME = INSTANCE.field(MainAutoIncIdTable.INSTANCE.name);

    private AuditedWithInvalidFieldLevelValueFormatterType() {
        super("AuditedWithInvalidFieldValueFormatter");
    }
}
