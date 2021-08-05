package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.converters.IdentityValueConverter;
import com.kenshoo.pl.entity.internal.audit.MainTable;

import java.util.Objects;

@Audited
public class AuditedWithVirtualType extends AbstractType<AuditedWithVirtualType> {

    public static final AuditedWithVirtualType INSTANCE = new AuditedWithVirtualType();

    @Id
    public static final EntityField<AuditedWithVirtualType, Long> ID = INSTANCE.field(MainTable.INSTANCE.id);
    public static final EntityField<AuditedWithVirtualType, String> NAME = INSTANCE.field(MainTable.INSTANCE.name);
    public static final EntityField<AuditedWithVirtualType, String> DESC = INSTANCE.field(MainTable.INSTANCE.desc);
    public static final EntityField<AuditedWithVirtualType, String> VIRTUAL_DESC_1 = INSTANCE.virtualField(NAME,
                                                                                                           DESC,
                                                                                                           (s1, s2) -> s1 + "-" + s2,
                                                                                                           new IdentityValueConverter<>(String.class),
                                                                                                           Objects::equals);
    public static final EntityField<AuditedWithVirtualType, String> VIRTUAL_DESC_2 = INSTANCE.virtualField(NAME,
                                                                                                           DESC,
                                                                                                           (s1, s2) -> s1 + ":" + s2,
                                                                                                           new IdentityValueConverter<>(String.class),
                                                                                                           Objects::equals);

    private AuditedWithVirtualType() {
        super("AuditedWithVirtual");
    }
}
