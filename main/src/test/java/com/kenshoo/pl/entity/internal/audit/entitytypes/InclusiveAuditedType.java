package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.internal.audit.MainTable;

public class InclusiveAuditedType extends AbstractType<InclusiveAuditedType> {

    public static final InclusiveAuditedType INSTANCE = new InclusiveAuditedType();

    @Id
    public static final EntityField<InclusiveAuditedType, Long> ID = INSTANCE.field(MainTable.INSTANCE.id);
    @Audited
    public static final EntityField<InclusiveAuditedType, String> NAME = INSTANCE.field(MainTable.INSTANCE.name);
    @Audited
    public static final EntityField<InclusiveAuditedType, String> DESC = INSTANCE.field(MainTable.INSTANCE.desc);
    public static final EntityField<InclusiveAuditedType, String> DESC2 = INSTANCE.field(MainTable.INSTANCE.desc2);

    private InclusiveAuditedType() {
        super("InclusiveAudited");
    }
}
