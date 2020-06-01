package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.AbstractEntityType;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.internal.audit.MainWithAncestorTable;
import com.kenshoo.pl.entity.internal.audit.ancestorfieldsproviders.InvalidAuditedFieldsProvider;

@Audited(alwaysAuditedFieldsProvider = InvalidAuditedFieldsProvider.class)
public class AuditedWithInvalidFieldsProviderType extends AbstractEntityType<AuditedWithInvalidFieldsProviderType> {

    public static final AuditedWithInvalidFieldsProviderType INSTANCE = new AuditedWithInvalidFieldsProviderType();

    @Id
    public static final EntityField<AuditedWithInvalidFieldsProviderType, Long> ID = INSTANCE.field(MainWithAncestorTable.INSTANCE.id);
    public static final EntityField<AuditedWithInvalidFieldsProviderType, String> NAME = INSTANCE.field(MainWithAncestorTable.INSTANCE.name);
    public static final EntityField<AuditedWithInvalidFieldsProviderType, String> DESC = INSTANCE.field(MainWithAncestorTable.INSTANCE.desc);
    public static final EntityField<AuditedWithInvalidFieldsProviderType, String> DESC2 = INSTANCE.field(MainWithAncestorTable.INSTANCE.desc2);

    private AuditedWithInvalidFieldsProviderType() {
        super("AuditedWithInvalidFieldsProvider");
    }

    @Override
    public DataTable getPrimaryTable() {
        return MainWithAncestorTable.INSTANCE;
    }
}
