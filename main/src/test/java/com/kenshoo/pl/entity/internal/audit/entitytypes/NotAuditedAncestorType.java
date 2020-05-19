package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.internal.audit.AncestorTable;

public class NotAuditedAncestorType extends AbstractType<NotAuditedAncestorType> {

    public static final NotAuditedAncestorType INSTANCE = new NotAuditedAncestorType();

    @Id
    public static final EntityField<NotAuditedAncestorType, Long> ID = INSTANCE.field(AncestorTable.INSTANCE.id);
    public static final EntityField<NotAuditedAncestorType, String> NAME = INSTANCE.field(AncestorTable.INSTANCE.name);
    public static final EntityField<NotAuditedAncestorType, String> DESC = INSTANCE.field(AncestorTable.INSTANCE.desc);

    private NotAuditedAncestorType() {
        super("NotAuditedAncestor");
    }
}
