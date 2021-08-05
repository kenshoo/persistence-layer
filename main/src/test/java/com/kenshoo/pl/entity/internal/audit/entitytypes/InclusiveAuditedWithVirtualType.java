package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.converters.IdentityValueConverter;
import com.kenshoo.pl.entity.internal.audit.MainTable;

import java.util.Objects;

public class InclusiveAuditedWithVirtualType extends AbstractType<InclusiveAuditedWithVirtualType> {

    public static final InclusiveAuditedWithVirtualType INSTANCE = new InclusiveAuditedWithVirtualType();

    @Id
    public static final EntityField<InclusiveAuditedWithVirtualType, Long> ID = INSTANCE.field(MainTable.INSTANCE.id);
    @Audited
    public static final EntityField<InclusiveAuditedWithVirtualType, String> NAME = INSTANCE.field(MainTable.INSTANCE.name);
    @Audited
    public static final EntityField<InclusiveAuditedWithVirtualType, String> DESC = INSTANCE.field(MainTable.INSTANCE.desc);
    @Audited
    public static final EntityField<InclusiveAuditedWithVirtualType, String> VIRTUAL_DESC = INSTANCE.virtualField(NAME,
                                                                                                                  DESC,
                                                                                                                  (s1, s2) -> s1 + "-" + s2,
                                                                                                                  new IdentityValueConverter<>(String.class),
                                                                                                                  Objects::equals);

    private InclusiveAuditedWithVirtualType() {
        super("InclusiveAuditedWithVirtual");
    }
}
