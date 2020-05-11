package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.Audited;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.internal.audit.MainTable;

@Audited
public class AuditedWithoutDataFieldsType extends AbstractType<AuditedWithoutDataFieldsType> {

    public static final AuditedWithoutDataFieldsType INSTANCE = new AuditedWithoutDataFieldsType();

    @Id
    public static final EntityField<AuditedWithoutDataFieldsType, Long> ID = INSTANCE.field(MainTable.INSTANCE.id);

    private AuditedWithoutDataFieldsType() {
        super("AuditedWithoutDataFields");
    }
}
