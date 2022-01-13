package com.kenshoo.pl.entity.spi.audit;

import org.junit.Test;

import static com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType.AMOUNT;
import static com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType.AMOUNT3;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DBAuditFieldValueFormatterTest {

    private final DBAuditFieldValueFormatter formatter = new DBAuditFieldValueFormatter();

    @Test
    public void formatWhenHasCustomValueConverterShouldFormatValueCorrectly() {
        assertThat(formatter.format(AMOUNT3, 12.34), is("12"));
    }

    @Test
    public void formatWhenHasDefaultValueConverterShouldReturnStringRepresentation() {
        assertThat(formatter.format(AMOUNT, 12.34), is("12.34"));
    }
}