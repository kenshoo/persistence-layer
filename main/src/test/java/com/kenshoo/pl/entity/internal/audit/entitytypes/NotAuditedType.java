package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.internal.audit.MainTable;

public class NotAuditedType extends AbstractType<NotAuditedType> {

    public static final NotAuditedType INSTANCE = new NotAuditedType();

    @Id
    public static final EntityField<NotAuditedType, Long> ID = INSTANCE.field(MainTable.INSTANCE.id);
    public static final EntityField<NotAuditedType, String> NAME = INSTANCE.field(MainTable.INSTANCE.name);
    public static final EntityField<NotAuditedType, String> DESC = INSTANCE.field(MainTable.INSTANCE.desc);
    public static final EntityField<NotAuditedType, String> DESC2 = INSTANCE.field(MainTable.INSTANCE.desc2);

    private NotAuditedType() {
        super("NotAudited");
    }
}
