package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.internal.audit.ChildAutoIncIdTable;

public class NotAuditedChildType extends AbstractAutoIncIdChildType<NotAuditedChildType> {

    public static final NotAuditedChildType INSTANCE = new NotAuditedChildType();

    @Id
    public static final EntityField<NotAuditedChildType, Long> ID = INSTANCE.field(ChildAutoIncIdTable.INSTANCE.id);
    public static final EntityField<NotAuditedChildType, Long> PARENT_ID = INSTANCE.field(ChildAutoIncIdTable.INSTANCE.parent_id);
    public static final EntityField<NotAuditedChildType, String> NAME = INSTANCE.field(ChildAutoIncIdTable.INSTANCE.name);
    public static final EntityField<NotAuditedChildType, String> DESC = INSTANCE.field(ChildAutoIncIdTable.INSTANCE.desc);

    private NotAuditedChildType() {
        super("Child");
    }
}
