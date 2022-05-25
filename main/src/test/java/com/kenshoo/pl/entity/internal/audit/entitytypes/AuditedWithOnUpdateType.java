package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.internal.audit.MainAutoIncIdTable;

import static com.kenshoo.pl.entity.audit.AuditTrigger.ON_UPDATE;

@Audited
public class AuditedWithOnUpdateType extends AbstractAutoIncIdType<AuditedWithOnUpdateType> {

    public static final AuditedWithOnUpdateType INSTANCE = new AuditedWithOnUpdateType();

    @Id
    public static final EntityField<AuditedWithOnUpdateType, Long> ID = INSTANCE.field(MainAutoIncIdTable.INSTANCE.id);
    @Audited(trigger = ON_UPDATE)
    public static final EntityField<AuditedWithOnUpdateType, String> NAME = INSTANCE.field(MainAutoIncIdTable.INSTANCE.name);
    @Audited(trigger = ON_UPDATE)
    public static final EntityField<AuditedWithOnUpdateType, String> DESC = INSTANCE.field(MainAutoIncIdTable.INSTANCE.desc);
    @Audited(trigger = ON_UPDATE)
    public static final EntityField<AuditedWithOnUpdateType, String> DESC2 = INSTANCE.field(MainAutoIncIdTable.INSTANCE.desc2);

    private AuditedWithOnUpdateType() {
        super("AuditedWithOnUpdate");
    }
}
