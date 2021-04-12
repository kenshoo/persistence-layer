package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.AbstractEntityType;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.annotation.Required;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.annotation.audit.NotAudited;
import com.kenshoo.pl.entity.internal.audit.MainWithAncestorTable;
import com.kenshoo.pl.entity.internal.audit.ancestorfieldsproviders.AncestorWithFieldNameOverridesAuditExtensions;

import static com.kenshoo.pl.entity.annotation.RequiredFieldType.RELATION;

@Audited(extensions = AncestorWithFieldNameOverridesAuditExtensions.class)
public class AuditedWithAncestorFieldNameOverridesType extends AbstractEntityType<AuditedWithAncestorFieldNameOverridesType> {

    public static final AuditedWithAncestorFieldNameOverridesType INSTANCE = new AuditedWithAncestorFieldNameOverridesType();

    @Id
    public static final EntityField<AuditedWithAncestorFieldNameOverridesType, Long> ID = INSTANCE.field(MainWithAncestorTable.INSTANCE.id);
    @NotAudited
    @Required(RELATION)
    public static final EntityField<AuditedWithAncestorFieldNameOverridesType, Long> ANCESTOR_ID = INSTANCE.field(MainWithAncestorTable.INSTANCE.ancestor_id);
    public static final EntityField<AuditedWithAncestorFieldNameOverridesType, String> NAME = INSTANCE.field(MainWithAncestorTable.INSTANCE.name);

    private AuditedWithAncestorFieldNameOverridesType() {
        super("AuditedWithAncestorFieldNameOverrides");
    }

    @Override
    public DataTable getPrimaryTable() {
        return MainWithAncestorTable.INSTANCE;
    }
}
