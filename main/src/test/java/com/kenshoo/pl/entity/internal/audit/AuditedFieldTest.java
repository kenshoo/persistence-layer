package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.pl.entity.Entity;
import com.kenshoo.pl.entity.Triptional;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

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

    private final AuditedField<AuditedType, String> auditedDescField = new AuditedField<>(DESC, DESC_FIELD_NAME);

    @Test
    public void getUnderlying() {
        assertThat(auditedDescField.getField(), is(DESC));
    }

    @Test
    public void oneArgCtorAndGetName() {
        assertThat(new AuditedField<>(DESC).getName(), is(DESC.toString()));
    }

    @Test
    public void twoArgCtorAndGetName() {
        assertThat(auditedDescField.getName(), is(DESC_FIELD_NAME));
    }

    @Test
    public void getValueWhenNonNull() {
        when(entity.safeGet(DESC)).thenReturn(Triptional.of(DESC_VALUE));
        assertThat(auditedDescField.getValue(entity), is(Triptional.of(DESC_VALUE)));
    }

    @Test
    public void getValueWhenNull() {
        when(entity.safeGet(DESC)).thenReturn(Triptional.nullInstance());
        assertThat(auditedDescField.getValue(entity), is(Triptional.nullInstance()));
    }

    @Test
    public void getValueWhenAbsent() {
        when(entity.safeGet(DESC)).thenReturn(Triptional.absent());
        assertThat(auditedDescField.getValue(entity), is(Triptional.absent()));
    }

    @Test
    public void valuesEqualWhenEqual() {
        assertThat(auditedDescField.valuesEqual("abc", "abc"), is(true));
    }

    @Test
    public void valuesEqualWhenNotEqual() {
        assertThat(auditedDescField.valuesEqual("abc", "def"), is(false));
    }
}