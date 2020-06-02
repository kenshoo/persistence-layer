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
import com.kenshoo.pl.entity.internal.audit.ancestorfieldsproviders.AncestorMandatoryFieldsProvider;

import static com.kenshoo.pl.entity.annotation.RequiredFieldType.RELATION;

@Audited(mandatoryFieldsProvider = AncestorMandatoryFieldsProvider.class)
public class AuditedWithAncestorMandatoryType extends AbstractEntityType<AuditedWithAncestorMandatoryType> {

    public static final AuditedWithAncestorMandatoryType INSTANCE = new AuditedWithAncestorMandatoryType();

    @Id
    public static final EntityField<AuditedWithAncestorMandatoryType, Long> ID = INSTANCE.field(MainWithAncestorTable.INSTANCE.id);
    @NotAudited
    @Required(RELATION)
    public static final EntityField<AuditedWithAncestorMandatoryType, Long> ANCESTOR_ID = INSTANCE.field(MainWithAncestorTable.INSTANCE.ancestor_id);
    public static final EntityField<AuditedWithAncestorMandatoryType, String> NAME = INSTANCE.field(MainWithAncestorTable.INSTANCE.name);
    public static final EntityField<AuditedWithAncestorMandatoryType, String> DESC = INSTANCE.field(MainWithAncestorTable.INSTANCE.desc);
    public static final EntityField<AuditedWithAncestorMandatoryType, String> DESC2 = INSTANCE.field(MainWithAncestorTable.INSTANCE.desc2);

    private AuditedWithAncestorMandatoryType() {
        super("AuditedWithAncestorMandatory");
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
