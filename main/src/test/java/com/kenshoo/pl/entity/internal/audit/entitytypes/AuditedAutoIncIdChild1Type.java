package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.internal.audit.ChildAutoIncIdTable;

@Audited
public class AuditedAutoIncIdChild1Type extends AbstractAutoIncIdChildType<AuditedAutoIncIdChild1Type> {

    public static final AuditedAutoIncIdChild1Type INSTANCE = new AuditedAutoIncIdChild1Type();

    @Id
    public static final EntityField<AuditedAutoIncIdChild1Type, Long> ID = INSTANCE.field(ChildAutoIncIdTable.INSTANCE.id);
    public static final EntityField<AuditedAutoIncIdChild1Type, Long> PARENT_ID = INSTANCE.field(ChildAutoIncIdTable.INSTANCE.parent_id);
    public static final EntityField<AuditedAutoIncIdChild1Type, String> NAME = INSTANCE.field(ChildAutoIncIdTable.INSTANCE.name);
    public static final EntityField<AuditedAutoIncIdChild1Type, String> DESC = INSTANCE.field(ChildAutoIncIdTable.INSTANCE.desc);

    private AuditedAutoIncIdChild1Type() {
        super("AuditedChild1");
    }
}
