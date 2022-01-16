package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.internal.audit.MainTable;
import com.kenshoo.pl.entity.internal.audit.formatters.CustomAuditFieldValueFormatter1;

@Audited
public class AuditedWithFieldLevelOnlyValueFormatterType extends AbstractType<AuditedWithFieldLevelOnlyValueFormatterType> {

    public static final AuditedWithFieldLevelOnlyValueFormatterType INSTANCE = new AuditedWithFieldLevelOnlyValueFormatterType();

    @Id
    public static final EntityField<AuditedWithFieldLevelOnlyValueFormatterType, Long> ID = INSTANCE.field(MainTable.INSTANCE.id);
    @Audited(valueFormatter = CustomAuditFieldValueFormatter1.class)
    public static final EntityField<AuditedWithFieldLevelOnlyValueFormatterType, String> NAME = INSTANCE.field(MainTable.INSTANCE.name);

    private AuditedWithFieldLevelOnlyValueFormatterType() {
        super("AuditedWithFieldLevelOnlyValueFormatter");
    }
}
