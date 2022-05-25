package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.internal.audit.MainAutoIncIdTable;

public class InclusiveAuditedType extends AbstractAutoIncIdType<InclusiveAuditedType> {

    public static final InclusiveAuditedType INSTANCE = new InclusiveAuditedType();

    @Id
    public static final EntityField<InclusiveAuditedType, Long> ID = INSTANCE.field(MainAutoIncIdTable.INSTANCE.id);
    @Audited
    public static final EntityField<InclusiveAuditedType, String> NAME = INSTANCE.field(MainAutoIncIdTable.INSTANCE.name);
    @Audited
    public static final EntityField<InclusiveAuditedType, String> DESC = INSTANCE.field(MainAutoIncIdTable.INSTANCE.desc);
    public static final EntityField<InclusiveAuditedType, String> DESC2 = INSTANCE.field(MainAutoIncIdTable.INSTANCE.desc2);

    private InclusiveAuditedType() {
        super("InclusiveAudited");
    }
}
