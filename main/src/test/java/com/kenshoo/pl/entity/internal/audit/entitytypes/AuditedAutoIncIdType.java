package com.kenshoo.pl.entity.internal.audit.entitytypes;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.converters.IdentityValueConverter;
import com.kenshoo.pl.entity.internal.audit.MainAutoIncIdTable;
import com.kenshoo.pl.entity.internal.audit.converters.DoubleToIntegerValueConverter;
import com.kenshoo.pl.entity.internal.audit.converters.DoubleToStringValueConverter;

import static java.lang.Math.abs;

@Audited
public class AuditedAutoIncIdType extends AbstractAutoIncIdType<AuditedAutoIncIdType> {

    public static final AuditedAutoIncIdType INSTANCE = new AuditedAutoIncIdType();

    @Id
    public static final EntityField<AuditedAutoIncIdType, Long> ID = INSTANCE.field(MainAutoIncIdTable.INSTANCE.id);
    public static final EntityField<AuditedAutoIncIdType, String> NAME = INSTANCE.field(MainAutoIncIdTable.INSTANCE.name);
    public static final EntityField<AuditedAutoIncIdType, String> DESC = INSTANCE.field(MainAutoIncIdTable.INSTANCE.desc);
    public static final EntityField<AuditedAutoIncIdType, String> DESC2 = INSTANCE.field(MainAutoIncIdTable.INSTANCE.desc2);
    public static final EntityField<AuditedAutoIncIdType, Double> AMOUNT = INSTANCE.field(MainAutoIncIdTable.INSTANCE.amount, (a1, a2) -> abs(a1 - a2) < 0.01);
    public static final EntityField<AuditedAutoIncIdType, Double> AMOUNT2 = INSTANCE.field(MainAutoIncIdTable.INSTANCE.amount2,
                                                                                  IdentityValueConverter.getInstance(Double.class),
                                                                                  new DoubleToStringValueConverter(),
                                                                                  (a1, a2) -> abs(a1 - a2) < 0.01);
    public static final EntityField<AuditedAutoIncIdType, Double> AMOUNT3 = INSTANCE.field(MainAutoIncIdTable.INSTANCE.amount3,
                                                                                  new DoubleToIntegerValueConverter());

    private AuditedAutoIncIdType() {
        super("AuditedAutoInc");
    }
}
