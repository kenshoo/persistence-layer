package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.internal.audit.MainTable;
import com.kenshoo.pl.entity.internal.audit.formatters.CustomAuditFieldValueFormatter1;
import com.kenshoo.pl.entity.internal.audit.formatters.CustomAuditFieldValueFormatter2;

@Audited(valueFormatter = CustomAuditFieldValueFormatter1.class)
public class AuditedWithEntityLevelValueFormatterType extends AbstractType<AuditedWithEntityLevelValueFormatterType> {

    public static final AuditedWithEntityLevelValueFormatterType INSTANCE = new AuditedWithEntityLevelValueFormatterType();

    @Id
    public static final EntityField<AuditedWithEntityLevelValueFormatterType, Long> ID = INSTANCE.field(MainTable.INSTANCE.id);
    @Audited
    public static final EntityField<AuditedWithEntityLevelValueFormatterType, String> NAME = INSTANCE.field(MainTable.INSTANCE.name);
    @Audited(valueFormatter = CustomAuditFieldValueFormatter2.class)
    public static final EntityField<AuditedWithEntityLevelValueFormatterType, String> DESC = INSTANCE.field(MainTable.INSTANCE.desc);
    public static final EntityField<AuditedWithEntityLevelValueFormatterType, String> DESC2 = INSTANCE.field(MainTable.INSTANCE.desc2);

    private AuditedWithEntityLevelValueFormatterType() {
        super("AuditedWithEntityLevelValueFormatter");
    }
}
