package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.internal.audit.ChildTable;

public class NotAuditedChildType extends AbstractChildType<NotAuditedChildType> {

    public static final NotAuditedChildType INSTANCE = new NotAuditedChildType();

    @Id
    public static final EntityField<NotAuditedChildType, Long> ID = INSTANCE.field(ChildTable.INSTANCE.id);
    public static final EntityField<NotAuditedChildType, Long> PARENT_ID = INSTANCE.field(ChildTable.INSTANCE.parent_id);
    public static final EntityField<NotAuditedChildType, String> NAME = INSTANCE.field(ChildTable.INSTANCE.name);
    public static final EntityField<NotAuditedChildType, String> DESC = INSTANCE.field(ChildTable.INSTANCE.desc);

    private NotAuditedChildType() {
        super("Child");
    }
}
