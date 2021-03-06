package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.converters.IdentityValueConverter;
import com.kenshoo.pl.entity.internal.audit.MainTable;
import com.kenshoo.pl.entity.internal.audit.converters.DoubleToStringValueConverter;

import static java.lang.Math.abs;

@Audited
public class AuditedType extends AbstractType<AuditedType> {

    public static final AuditedType INSTANCE = new AuditedType();

    @Id
    public static final EntityField<AuditedType, Long> ID = INSTANCE.field(MainTable.INSTANCE.id);
    public static final EntityField<AuditedType, String> NAME = INSTANCE.field(MainTable.INSTANCE.name);
    public static final EntityField<AuditedType, String> DESC = INSTANCE.field(MainTable.INSTANCE.desc);
    public static final EntityField<AuditedType, String> DESC2 = INSTANCE.field(MainTable.INSTANCE.desc2);
    public static final EntityField<AuditedType, Double> AMOUNT = INSTANCE.field(MainTable.INSTANCE.amount, (a1, a2) -> abs(a1 - a2) < 0.01);
    public static final EntityField<AuditedType, Double> AMOUNT2 = INSTANCE.field(MainTable.INSTANCE.amount2,
                                                                                  IdentityValueConverter.getInstance(Double.class),
                                                                                  new DoubleToStringValueConverter(),
                                                                                  (a1, a2) -> abs(a1 - a2) < 0.01);

    private AuditedType() {
        super("Audited");
    }
}
