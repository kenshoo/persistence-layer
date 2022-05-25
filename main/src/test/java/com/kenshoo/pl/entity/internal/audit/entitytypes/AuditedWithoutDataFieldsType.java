package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.internal.audit.MainAutoIncIdTable;

@Audited
public class AuditedWithoutDataFieldsType extends AbstractAutoIncIdType<AuditedWithoutDataFieldsType> {

    public static final AuditedWithoutDataFieldsType INSTANCE = new AuditedWithoutDataFieldsType();

    @Id
    public static final EntityField<AuditedWithoutDataFieldsType, Long> ID = INSTANCE.field(MainAutoIncIdTable.INSTANCE.id);

    private AuditedWithoutDataFieldsType() {
        super("AuditedWithoutDataFields");
    }
}
