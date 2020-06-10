package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.AbstractEntityType;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.internal.audit.MainWithAncestorTable;
import com.kenshoo.pl.entity.internal.audit.ancestorfieldsproviders.InvalidAuditExtensions;

@Audited(extensions = InvalidAuditExtensions.class)
public class AuditedWithInvalidExtensionsType extends AbstractEntityType<AuditedWithInvalidExtensionsType> {

    public static final AuditedWithInvalidExtensionsType INSTANCE = new AuditedWithInvalidExtensionsType();

    @Id
    public static final EntityField<AuditedWithInvalidExtensionsType, Long> ID = INSTANCE.field(MainWithAncestorTable.INSTANCE.id);
    public static final EntityField<AuditedWithInvalidExtensionsType, String> NAME = INSTANCE.field(MainWithAncestorTable.INSTANCE.name);
    public static final EntityField<AuditedWithInvalidExtensionsType, String> DESC = INSTANCE.field(MainWithAncestorTable.INSTANCE.desc);
    public static final EntityField<AuditedWithInvalidExtensionsType, String> DESC2 = INSTANCE.field(MainWithAncestorTable.INSTANCE.desc2);

    private AuditedWithInvalidExtensionsType() {
        super("AuditedWithInvalidExtensions");
    }

    @Override
    public DataTable getPrimaryTable() {
        return MainWithAncestorTable.INSTANCE;
    }
}
