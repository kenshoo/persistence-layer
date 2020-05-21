package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.internal.audit.ChildTable;

@Audited
public class AuditedChild1Type extends AbstractChildType<AuditedChild1Type> {

    public static final AuditedChild1Type INSTANCE = new AuditedChild1Type();

    @Id
    public static final EntityField<AuditedChild1Type, Long> ID = INSTANCE.field(ChildTable.INSTANCE.id);
    public static final EntityField<AuditedChild1Type, Long> PARENT_ID = INSTANCE.field(ChildTable.INSTANCE.parent_id);
    public static final EntityField<AuditedChild1Type, String> NAME = INSTANCE.field(ChildTable.INSTANCE.name);
    public static final EntityField<AuditedChild1Type, String> DESC = INSTANCE.field(ChildTable.INSTANCE.desc);

    private AuditedChild1Type() {
        super("AuditedChild1");
    }
}
