package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.pl.entity.CommonTypesStringConverter;
import com.kenshoo.pl.entity.internal.audit.converters.DoubleToStringValueConverter;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static com.kenshoo.pl.entity.audit.AuditTrigger.ON_CREATE_OR_UPDATE;
import static com.kenshoo.pl.entity.audit.AuditTrigger.ON_UPDATE;
import static com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType.AMOUNT2;
import static com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType.DESC;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class AuditedFieldTest {

    private static final String DESC_FIELD_NAME = "desc";

    private final AuditedField<AuditedType, String> fullAuditedDescField = AuditedField.builder(DESC)
                                                                                       .withName(DESC_FIELD_NAME)
                                                                                       .withTrigger(ON_UPDATE)
                                                                                       .build();
    private final AuditedField<AuditedType, String> minimalAuditedDescField = AuditedField.builder(DESC).build();

    private final AuditedField<AuditedType, Double> auditedFormattedAmountField = AuditedField.builder(AMOUNT2).build();

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
    public void getStringValueConverterWhenSpecified() {
        assertThat("Incorrect string value converter for " + auditedFormattedAmountField + ": ",
                   auditedFormattedAmountField.getStringValueConverter(),
                   instanceOf(DoubleToStringValueConverter.class));
    }

    @Test
    public void getStringValueConverterWhenDefault() {
        assertThat(fullAuditedDescField.getStringValueConverter(),
                   instanceOf(CommonTypesStringConverter.class));
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