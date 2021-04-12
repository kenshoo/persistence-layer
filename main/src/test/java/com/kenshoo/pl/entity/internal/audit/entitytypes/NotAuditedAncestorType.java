package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.AbstractEntityType;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.converters.IdentityValueConverter;
import com.kenshoo.pl.entity.internal.audit.AncestorTable;
import com.kenshoo.pl.entity.internal.audit.converters.DoubleToStringValueConverter;

import static java.lang.Math.abs;

public class NotAuditedAncestorType extends AbstractEntityType<NotAuditedAncestorType> {

    public static final NotAuditedAncestorType INSTANCE = new NotAuditedAncestorType();

    @Id
    public static final EntityField<NotAuditedAncestorType, Long> ID = INSTANCE.field(AncestorTable.INSTANCE.id);
    public static final EntityField<NotAuditedAncestorType, String> NAME = INSTANCE.field(AncestorTable.INSTANCE.name);
    public static final EntityField<NotAuditedAncestorType, String> DESC = INSTANCE.field(AncestorTable.INSTANCE.desc);
    public static final EntityField<NotAuditedAncestorType, Double> AMOUNT = INSTANCE.field(AncestorTable.INSTANCE.amount,
                                                                                            (a1, a2) -> abs(a1 - a2) < 0.01);
    public static final EntityField<NotAuditedAncestorType, Double> AMOUNT2 = INSTANCE.field(AncestorTable.INSTANCE.amount2,
                                                                                             IdentityValueConverter.getInstance(Double.class),
                                                                                             new DoubleToStringValueConverter(),
                                                                                             (a1, a2) -> abs(a1 - a2) < 0.01);

    private NotAuditedAncestorType() {
        super("NotAuditedAncestor");
    }

    @Override
    public DataTable getPrimaryTable() {
        return AncestorTable.INSTANCE;
    }
}
