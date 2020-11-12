package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.internal.audit.MainTable;

import static com.kenshoo.pl.entity.audit.AuditTrigger.ON_UPDATE;

@Audited
public class AuditedWithOnUpdateType extends AbstractType<AuditedWithOnUpdateType> {

    public static final AuditedWithOnUpdateType INSTANCE = new AuditedWithOnUpdateType();

    @Id
    public static final EntityField<AuditedWithOnUpdateType, Long> ID = INSTANCE.field(MainTable.INSTANCE.id);
    @Audited(trigger = ON_UPDATE)
    public static final EntityField<AuditedWithOnUpdateType, String> NAME = INSTANCE.field(MainTable.INSTANCE.name);
    @Audited(trigger = ON_UPDATE)
    public static final EntityField<AuditedWithOnUpdateType, String> DESC = INSTANCE.field(MainTable.INSTANCE.desc);
    @Audited(trigger = ON_UPDATE)
    public static final EntityField<AuditedWithOnUpdateType, String> DESC2 = INSTANCE.field(MainTable.INSTANCE.desc2);

    private AuditedWithOnUpdateType() {
        super("AuditedWithOnUpdate");
    }
}
