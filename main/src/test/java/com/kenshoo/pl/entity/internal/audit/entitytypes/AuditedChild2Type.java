package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.internal.audit.ChildTable;

@Audited
public class AuditedChild2Type extends AbstractChildType<AuditedChild2Type> {

    public static final AuditedChild2Type INSTANCE = new AuditedChild2Type();

    @Id
    public static final EntityField<AuditedChild2Type, Long> ID = INSTANCE.field(ChildTable.INSTANCE.id);
    public static final EntityField<AuditedChild2Type, Long> PARENT_ID = INSTANCE.field(ChildTable.INSTANCE.parent_id);
    public static final EntityField<AuditedChild2Type, String> NAME = INSTANCE.field(ChildTable.INSTANCE.name);
    public static final EntityField<AuditedChild2Type, String> DESC = INSTANCE.field(ChildTable.INSTANCE.desc);

    private AuditedChild2Type() {
        super("AuditedChild2");
    }
}
