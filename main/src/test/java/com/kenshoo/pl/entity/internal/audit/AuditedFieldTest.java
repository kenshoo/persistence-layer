package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedAutoIncIdType;
import com.kenshoo.pl.entity.spi.audit.AuditFieldValueFormatter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.kenshoo.pl.entity.audit.AuditTrigger.ON_CREATE_OR_UPDATE;
import static com.kenshoo.pl.entity.audit.AuditTrigger.ON_UPDATE;
import static com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedAutoIncIdType.AMOUNT2;
import static com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedAutoIncIdType.DESC;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuditedFieldTest {

    private static final String DESC_FIELD_NAME = "desc";
    private static final String DESC_FIELD_VALUE = "descValue";
    private static final String DESC_FIELD_FORMATTED_VALUE = "descValueFormatted";

    @Mock
    private AuditFieldValueFormatter customValueFormatter;

    private AuditedField<AuditedAutoIncIdType, String> fullAuditedDescField;
    private AuditedField<AuditedAutoIncIdType, String> minimalAuditedDescField;
    private AuditedField<AuditedAutoIncIdType, Double> auditedFormattedAmountField;

    @Before
    public void setUp() {
        fullAuditedDescField = AuditedField.builder(DESC)
                                           .withName(DESC_FIELD_NAME)
                                           .withValueFormatter(customValueFormatter)
                                           .withTrigger(ON_UPDATE)
                                           .build();
        minimalAuditedDescField = AuditedField.builder(DESC)
                                              .build();

        auditedFormattedAmountField = AuditedField.builder(AMOUNT2).build();
    }

    @Test
    public void getField() {
        assertThat(fullAuditedDescField.getField(), is(DESC));
    }

    @Test
    public void minimalObjGetNameShouldReturnDefaultName() {
        assertThat(minimalAuditedDescField.getName(), is(DESC.toString()));
    }

    @Test
    public void fullObjGetNameShouldReturnInputName() {
        assertThat(fullAuditedDescField.getName(), is(DESC_FIELD_NAME));
    }

    @Test
    public void minimalObjGetTriggerShouldReturnOnCreateOrUpdate() {
        assertThat(minimalAuditedDescField.getTrigger(), is(ON_CREATE_OR_UPDATE));
    }

    @Test
    public void fullObjGetTriggerShouldReturnInputTrigger() {
        assertThat(fullAuditedDescField.getTrigger(), is(ON_UPDATE));
    }

    @Test
    public void formatValueWhenCustomFormatterProvidedShouldApplyIt() {
        when(customValueFormatter.format(DESC, DESC_FIELD_VALUE)).thenReturn(DESC_FIELD_FORMATTED_VALUE);

        assertThat(fullAuditedDescField.formatValue(DESC_FIELD_VALUE), is(DESC_FIELD_FORMATTED_VALUE));
    }

    @Test
    public void formatDoubleValueWhenNoFormatterProvidedShouldApplyStringFormatterOfField() {
        assertThat(auditedFormattedAmountField.formatValue(12.343), is("12.34"));
    }

    @Test
    public void valuesEqualWhenEqual() {
        assertThat(fullAuditedDescField.valuesEqual("abc", "abc"), is(true));
    }

    @Test
    public void valuesEqualWhenNotEqual() {
        assertThat(fullAuditedDescField.valuesEqual("abc", "def"), is(false));
    }
}