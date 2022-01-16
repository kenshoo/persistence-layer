package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.internal.audit.MainTable;
import com.kenshoo.pl.entity.internal.audit.formatters.InvalidAuditFieldValueFormatter;

@Audited(valueFormatter = InvalidAuditFieldValueFormatter.class)
public class AuditedWithInvalidEntityLevelValueFormatterType extends AbstractType<AuditedWithInvalidEntityLevelValueFormatterType> {

    public static final AuditedWithInvalidEntityLevelValueFormatterType INSTANCE = new AuditedWithInvalidEntityLevelValueFormatterType();

    @Id
    public static final EntityField<AuditedWithInvalidEntityLevelValueFormatterType, Long> ID = INSTANCE.field(MainTable.INSTANCE.id);
    public static final EntityField<AuditedWithInvalidEntityLevelValueFormatterType, String> NAME = INSTANCE.field(MainTable.INSTANCE.name);

    private AuditedWithInvalidEntityLevelValueFormatterType() {
        super("AuditedWithInvalidEntityValueFormatter");
    }
}
