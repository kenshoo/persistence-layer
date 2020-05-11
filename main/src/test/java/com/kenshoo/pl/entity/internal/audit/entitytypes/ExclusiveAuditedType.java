package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.Audited;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.annotation.NotAudited;
import com.kenshoo.pl.entity.internal.audit.MainTable;

@Audited
public class ExclusiveAuditedType extends AbstractType<ExclusiveAuditedType> {

    public static final ExclusiveAuditedType INSTANCE = new ExclusiveAuditedType();

    @Id
    public static final EntityField<ExclusiveAuditedType, Long> ID = INSTANCE.field(MainTable.INSTANCE.id);
    public static final EntityField<ExclusiveAuditedType, String> NAME = INSTANCE.field(MainTable.INSTANCE.name);
    @NotAudited
    public static final EntityField<ExclusiveAuditedType, String> DESC = INSTANCE.field(MainTable.INSTANCE.desc);
    @NotAudited
    public static final EntityField<ExclusiveAuditedType, String> DESC2 = INSTANCE.field(MainTable.INSTANCE.desc2);

    private ExclusiveAuditedType() {
        super("ExclusiveAudited");
    }
}
