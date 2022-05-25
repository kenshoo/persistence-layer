package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.internal.audit.MainAutoIncIdTable;

public class NotAuditedType extends AbstractAutoIncIdType<NotAuditedType> {

    public static final NotAuditedType INSTANCE = new NotAuditedType();

    @Id
    public static final EntityField<NotAuditedType, Long> ID = INSTANCE.field(MainAutoIncIdTable.INSTANCE.id);
    public static final EntityField<NotAuditedType, String> NAME = INSTANCE.field(MainAutoIncIdTable.INSTANCE.name);
    public static final EntityField<NotAuditedType, String> DESC = INSTANCE.field(MainAutoIncIdTable.INSTANCE.desc);
    public static final EntityField<NotAuditedType, String> DESC2 = INSTANCE.field(MainAutoIncIdTable.INSTANCE.desc2);

    private NotAuditedType() {
        super("NotAudited");
    }
}
