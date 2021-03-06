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

@Audited(extensions = AncestorAuditExtensions.class)
public class AuditedWithInternalAndAncestorMandatoryType extends AbstractEntityType<AuditedWithInternalAndAncestorMandatoryType> {

    public static final AuditedWithInternalAndAncestorMandatoryType INSTANCE = new AuditedWithInternalAndAncestorMandatoryType();

    @Id
    public static final EntityField<AuditedWithInternalAndAncestorMandatoryType, Long> ID = INSTANCE.field(MainWithAncestorTable.INSTANCE.id);
    @NotAudited
    @Required(RELATION)
    public static final EntityField<AuditedWithInternalAndAncestorMandatoryType, Long> ANCESTOR_ID = INSTANCE.field(MainWithAncestorTable.INSTANCE.ancestor_id);
    @Audited(trigger = ALWAYS)
    public static final EntityField<AuditedWithInternalAndAncestorMandatoryType, String> NAME = INSTANCE.field(MainWithAncestorTable.INSTANCE.name);
    public static final EntityField<AuditedWithInternalAndAncestorMandatoryType, String> DESC = INSTANCE.field(MainWithAncestorTable.INSTANCE.desc);
    public static final EntityField<AuditedWithInternalAndAncestorMandatoryType, String> DESC2 = INSTANCE.field(MainWithAncestorTable.INSTANCE.desc2);

    private AuditedWithInternalAndAncestorMandatoryType() {
        super("AuditedWithInternalAndAncestor");
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
