package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.AbstractEntityType;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.annotation.Required;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.annotation.audit.NotAudited;
import com.kenshoo.pl.entity.internal.audit.MainWithAncestorTable;
import com.kenshoo.pl.entity.internal.audit.ancestorfieldsproviders.AncestorWithFieldValueFormattersAuditExtensions;

import static com.kenshoo.pl.entity.annotation.RequiredFieldType.RELATION;

@Audited(extensions = AncestorWithFieldValueFormattersAuditExtensions.class)
public class AuditedWithAncestorValueFormattersType extends AbstractEntityType<AuditedWithAncestorValueFormattersType> {

    public static final AuditedWithAncestorValueFormattersType INSTANCE = new AuditedWithAncestorValueFormattersType();

    @Id
    public static final EntityField<AuditedWithAncestorValueFormattersType, Long> ID = INSTANCE.field(MainWithAncestorTable.INSTANCE.id);
    @NotAudited
    @Required(RELATION)
    public static final EntityField<AuditedWithAncestorValueFormattersType, Long> ANCESTOR_ID = INSTANCE.field(MainWithAncestorTable.INSTANCE.ancestor_id);
    public static final EntityField<AuditedWithAncestorValueFormattersType, String> NAME = INSTANCE.field(MainWithAncestorTable.INSTANCE.name);

    private AuditedWithAncestorValueFormattersType() {
        super("AuditedWithAncestorValueFormatters");
    }

    @Override
    public DataTable getPrimaryTable() {
        return MainWithAncestorTable.INSTANCE;
    }
}
