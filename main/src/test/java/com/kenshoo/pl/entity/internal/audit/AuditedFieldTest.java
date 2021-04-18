package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.pl.entity.Entity;
import com.kenshoo.pl.entity.Triptional;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.kenshoo.pl.entity.audit.AuditTrigger.ON_CREATE_OR_UPDATE;
import static com.kenshoo.pl.entity.audit.AuditTrigger.ON_UPDATE;
import static com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType.DESC;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuditedFieldTest {

    private static final String DESC_FIELD_NAME = "desc";
    private static final String DESC_VALUE = "A description";

    @Mock
    private Entity entity;

    private final AuditedField<AuditedType, String> fullAuditedField = AuditedField.builder(DESC)
                                                                                   .withName(DESC_FIELD_NAME)
                                                                                   .withTrigger(ON_UPDATE)
                                                                                   .build();
    private final AuditedField<AuditedType, String> minimalAuditedField = AuditedField.builder(DESC).build();

    @Test
    public void getField() {
        assertThat(fullAuditedField.getField(), is(DESC));
    }

    @Test
    public void minimalObjGetNameShouldReturnDefaultName() {
        assertThat(minimalAuditedField.getName(), is(DESC.toString()));
    }

    @Test
    public void fullObjGetNameShouldReturnInputName() {
        assertThat(fullAuditedField.getName(), is(DESC_FIELD_NAME));
    }

    @Test
    public void minimalObjGetTriggerShouldReturnOnCreateOrUpdate() {
        assertThat(minimalAuditedField.getTrigger(), is(ON_CREATE_OR_UPDATE));
    }

    @Test
    public void fullObjGetTriggerShouldReturnInputTrigger() {
        assertThat(fullAuditedField.getTrigger(), is(ON_UPDATE));
    }

    @Test
    public void getValueWhenNonNull() {
        when(entity.safeGet(DESC)).thenReturn(Triptional.of(DESC_VALUE));
        assertThat(fullAuditedField.getValue(entity), is(Triptional.of(DESC_VALUE)));
    }

    @Test
    public void getValueWhenNull() {
        when(entity.safeGet(DESC)).thenReturn(Triptional.nullInstance());
        assertThat(fullAuditedField.getValue(entity), is(Triptional.nullInstance()));
    }

    @Test
    public void getValueWhenAbsent() {
        when(entity.safeGet(DESC)).thenReturn(Triptional.absent());
        assertThat(fullAuditedField.getValue(entity), is(Triptional.absent()));
    }

    @Test
    public void valuesEqualWhenEqual() {
        assertThat(fullAuditedField.valuesEqual("abc", "abc"), is(true));
    }

    @Test
    public void valuesEqualWhenNotEqual() {
        assertThat(fullAuditedField.valuesEqual("abc", "def"), is(false));
    }
}