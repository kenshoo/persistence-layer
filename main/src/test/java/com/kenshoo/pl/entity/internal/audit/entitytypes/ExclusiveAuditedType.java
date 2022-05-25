package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.annotation.audit.NotAudited;
import com.kenshoo.pl.entity.internal.audit.MainAutoIncIdTable;

@Audited
public class ExclusiveAuditedType extends AbstractAutoIncIdType<ExclusiveAuditedType> {

    public static final ExclusiveAuditedType INSTANCE = new ExclusiveAuditedType();

    @Id
    public static final EntityField<ExclusiveAuditedType, Long> ID = INSTANCE.field(MainAutoIncIdTable.INSTANCE.id);
    public static final EntityField<ExclusiveAuditedType, String> NAME = INSTANCE.field(MainAutoIncIdTable.INSTANCE.name);
    @NotAudited
    public static final EntityField<ExclusiveAuditedType, String> DESC = INSTANCE.field(MainAutoIncIdTable.INSTANCE.desc);
    @NotAudited
    public static final EntityField<ExclusiveAuditedType, String> DESC2 = INSTANCE.field(MainAutoIncIdTable.INSTANCE.desc2);

    private ExclusiveAuditedType() {
        super("ExclusiveAudited");
    }
}
