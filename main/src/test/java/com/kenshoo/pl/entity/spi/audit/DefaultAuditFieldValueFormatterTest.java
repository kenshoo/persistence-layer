package com.kenshoo.pl.entity.spi.audit;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.internal.audit.AuditedField;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType;
import org.junit.Test;

import static com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType.AMOUNT;
import static com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType.AMOUNT2;
import static com.kenshoo.pl.entity.spi.audit.DefaultAuditFieldValueFormatter.INSTANCE;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DefaultAuditFieldValueFormatterTest {

    private static final double AMOUNT_VALUE = 12.1199999;
    private static final String NON_FORMATTED_AMOUNT_STRING_VALUE = "12.1199999";
    private static final String FORMATTED_AMOUNT_STRING_VALUE = "12.12";

    @Test
    public void formatWhenNonNullAndHasFormatter() {
        assertThat(INSTANCE.format(asAuditedField(AMOUNT2), AMOUNT_VALUE), is(FORMATTED_AMOUNT_STRING_VALUE));
    }

    @Test
    public void formatWhenNonNullAndDoesntHaveFormatter() {
        assertThat(INSTANCE.format(asAuditedField(AMOUNT), AMOUNT_VALUE), is(NON_FORMATTED_AMOUNT_STRING_VALUE));
    }

    @Test(expected = NullPointerException.class)
    public void formatWhenFieldNullShouldThrowNullPointerException() {
        INSTANCE.format(null, AMOUNT_VALUE);
    }

    @Test(expected = NullPointerException.class)
    public void formatWhenValueNullShouldThrowNullPointerException() {
        INSTANCE.format(asAuditedField(AMOUNT), null);
    }

    private AuditedField<AuditedType, Double> asAuditedField(EntityField<AuditedType, Double> entityField) {
        return AuditedField.builder(entityField).build();
    }
}