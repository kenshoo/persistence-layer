package com.kenshoo.pl.entity.spi.audit;

import org.junit.Test;

import static com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType.AMOUNT;
import static com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType.AMOUNT2;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DefaultAuditFieldValueFormatterTest {

    private final AuditFieldValueFormatter formatter = DefaultAuditFieldValueFormatter.INSTANCE;

    @Test
    public void formatWhenHasCustomStringValueConverterShouldFormatValueCorrectly() {
        assertThat(formatter.format(AMOUNT2, 12.3956), is("12.40"));
    }

    @Test
    public void formatWhenHasDefaultStringValueConverterShouldReturnStringRepresentation() {
        assertThat(formatter.format(AMOUNT, 12.3956), is("12.3956"));
    }

}