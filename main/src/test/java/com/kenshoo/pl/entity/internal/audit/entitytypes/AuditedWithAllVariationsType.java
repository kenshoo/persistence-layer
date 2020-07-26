package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.AbstractEntityType;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.SupportedChangeOperation;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.annotation.Required;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.annotation.audit.NotAudited;
import com.kenshoo.pl.entity.internal.audit.MainWithAncestorTable;
import com.kenshoo.pl.entity.internal.audit.ancestorfieldsproviders.AncestorAuditExtensions;

import static com.kenshoo.pl.entity.annotation.RequiredFieldType.RELATION;
import static com.kenshoo.pl.entity.audit.AuditTrigger.ALWAYS;
import static com.kenshoo.pl.entity.audit.AuditTrigger.ON_UPDATE;

@Audited(extensions = AncestorAuditExtensions.class)
public class AuditedWithAllVariationsType extends AbstractEntityType<AuditedWithAllVariationsType> {

    public static final AuditedWithAllVariationsType INSTANCE = new AuditedWithAllVariationsType();

    @Id
    public static final EntityField<AuditedWithAllVariationsType, Long> ID = INSTANCE.field(MainWithAncestorTable.INSTANCE.id);
    @NotAudited
    @Required(RELATION)
    public static final EntityField<AuditedWithAllVariationsType, Long> ANCESTOR_ID = INSTANCE.field(MainWithAncestorTable.INSTANCE.ancestor_id);
    @Audited(trigger = ALWAYS)
    public static final EntityField<AuditedWithAllVariationsType, String> NAME = INSTANCE.field(MainWithAncestorTable.INSTANCE.name);
    public static final EntityField<AuditedWithAllVariationsType, String> DESC = INSTANCE.field(MainWithAncestorTable.INSTANCE.desc);
    @Audited(trigger = ON_UPDATE)
    public static final EntityField<AuditedWithAllVariationsType, String> DESC2 = INSTANCE.field(MainWithAncestorTable.INSTANCE.desc2);

    private AuditedWithAllVariationsType() {
        super("AuditedWithAllVariations");
    }

    @Override
    public SupportedChangeOperation getSupportedOperation() {
        return SupportedChangeOperation.CREATE_UPDATE_AND_DELETE;
    }

    @Override
    public DataTable getPrimaryTable() {
        return MainWithAncestorTable.INSTANCE;
    }
}
