package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.internal.audit.ChildAutoIncIdTable;

@Audited
public class AuditedAutoIncIdChild2Type extends AbstractAutoIncIdChildType<AuditedAutoIncIdChild2Type> {

    public static final AuditedAutoIncIdChild2Type INSTANCE = new AuditedAutoIncIdChild2Type();

    @Id
    public static final EntityField<AuditedAutoIncIdChild2Type, Long> ID = INSTANCE.field(ChildAutoIncIdTable.INSTANCE.id);
    public static final EntityField<AuditedAutoIncIdChild2Type, Long> PARENT_ID = INSTANCE.field(ChildAutoIncIdTable.INSTANCE.parent_id);
    public static final EntityField<AuditedAutoIncIdChild2Type, String> NAME = INSTANCE.field(ChildAutoIncIdTable.INSTANCE.name);
    public static final EntityField<AuditedAutoIncIdChild2Type, String> DESC = INSTANCE.field(ChildAutoIncIdTable.INSTANCE.desc);

    private AuditedAutoIncIdChild2Type() {
        super("AuditedAutoIncChild2");
    }
}
