package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.AbstractEntityType;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.annotation.Required;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.annotation.audit.NotAudited;
import com.kenshoo.pl.entity.internal.audit.MainWithAncestorTable;
import com.kenshoo.pl.entity.internal.audit.ancestorfieldsproviders.AncestorSubEntitiesFieldsProvider;

import static com.kenshoo.pl.entity.annotation.RequiredFieldType.RELATION;

@Audited(alwaysAuditedFieldsProvider = AncestorSubEntitiesFieldsProvider.class)
public class AuditedWithAncestorAuditedFieldsType extends AbstractEntityType<AuditedWithAncestorAuditedFieldsType> {

    public static final AuditedWithAncestorAuditedFieldsType INSTANCE = new AuditedWithAncestorAuditedFieldsType();

    @Id
    public static final EntityField<AuditedWithAncestorAuditedFieldsType, Long> ID = INSTANCE.field(MainWithAncestorTable.INSTANCE.id);
    @NotAudited
    @Required(RELATION)
    public static final EntityField<AuditedWithAncestorAuditedFieldsType, Long> ANCESTOR_ID = INSTANCE.field(MainWithAncestorTable.INSTANCE.ancestor_id);
    public static final EntityField<AuditedWithAncestorAuditedFieldsType, String> NAME = INSTANCE.field(MainWithAncestorTable.INSTANCE.name);
    public static final EntityField<AuditedWithAncestorAuditedFieldsType, String> DESC = INSTANCE.field(MainWithAncestorTable.INSTANCE.desc);
    public static final EntityField<AuditedWithAncestorAuditedFieldsType, String> DESC2 = INSTANCE.field(MainWithAncestorTable.INSTANCE.desc2);

    private AuditedWithAncestorAuditedFieldsType() {
        super("AuditedWithAncestorFieldsProvider");
    }

    @Override
    public DataTable getPrimaryTable() {
        return MainWithAncestorTable.INSTANCE;
    }
}
