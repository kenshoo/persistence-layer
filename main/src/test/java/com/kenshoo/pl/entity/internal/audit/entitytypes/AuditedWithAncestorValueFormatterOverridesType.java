package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.AbstractEntityType;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.annotation.Required;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.annotation.audit.NotAudited;
import com.kenshoo.pl.entity.internal.audit.MainWithAncestorTable;
import com.kenshoo.pl.entity.internal.audit.ancestorfieldsproviders.AncestorWithFieldValueFormatterOverridesAuditExtensions;

import static com.kenshoo.pl.entity.annotation.RequiredFieldType.RELATION;

@Audited(extensions = AncestorWithFieldValueFormatterOverridesAuditExtensions.class)
public class AuditedWithAncestorValueFormatterOverridesType extends AbstractEntityType<AuditedWithAncestorValueFormatterOverridesType> {

    public static final AuditedWithAncestorValueFormatterOverridesType INSTANCE = new AuditedWithAncestorValueFormatterOverridesType();

    @Id
    public static final EntityField<AuditedWithAncestorValueFormatterOverridesType, Long> ID = INSTANCE.field(MainWithAncestorTable.INSTANCE.id);
    @NotAudited
    @Required(RELATION)
    public static final EntityField<AuditedWithAncestorValueFormatterOverridesType, Long> ANCESTOR_ID = INSTANCE.field(MainWithAncestorTable.INSTANCE.ancestor_id);
    public static final EntityField<AuditedWithAncestorValueFormatterOverridesType, String> NAME = INSTANCE.field(MainWithAncestorTable.INSTANCE.name);

    private AuditedWithAncestorValueFormatterOverridesType() {
        super("AuditedWithAncestorValueFormatterOverrides");
    }

    @Override
    public DataTable getPrimaryTable() {
        return MainWithAncestorTable.INSTANCE;
    }
}
