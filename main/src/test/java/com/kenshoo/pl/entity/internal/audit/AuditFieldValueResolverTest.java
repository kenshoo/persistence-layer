package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.pl.entity.Entity;
import com.kenshoo.pl.entity.Triptional;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.kenshoo.pl.entity.internal.audit.AuditFieldValueResolver.INSTANCE;
import static com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuditFieldValueResolverTest {

    private static final String DESC_VALUE = "A description";

    private static final double AMOUNT_VALUE = 12.1199999;
    private static final String NON_FORMATTED_AMOUNT_STRING_VALUE = "12.1199999";
    private static final String FORMATTED_AMOUNT_STRING_VALUE = "12.12";

    private final AuditedField<AuditedType, String> auditedDescField = AuditedField.builder(DESC).build();

    private final AuditedField<AuditedType, Double> auditedAmountField = AuditedField.builder(AMOUNT).build();
    private final AuditedField<AuditedType, Double> auditedFormattedAmountField = AuditedField.builder(AMOUNT2).build();

    @Mock
    private Entity entity;
    
    @Test
    public void resolveWhenNonNull() {
        when(entity.safeGet(DESC)).thenReturn(Triptional.of(DESC_VALUE));
        assertThat(INSTANCE.resolve(auditedDescField, entity), is(Triptional.of(DESC_VALUE)));
    }

    @Test
    public void resolveWhenNull() {
        when(entity.safeGet(DESC)).thenReturn(Triptional.nullInstance());
        assertThat(INSTANCE.resolve(auditedDescField, entity), is(Triptional.nullInstance()));
    }

    @Test
    public void resolveWhenAbsent() {
        when(entity.safeGet(DESC)).thenReturn(Triptional.absent());
        assertThat(INSTANCE.resolve(auditedDescField, entity), is(Triptional.absent()));
    }

    @Test
    public void resolveToStringWhenNonNullAndHasFormatter() {
        when(entity.safeGet(AMOUNT2)).thenReturn(Triptional.of(AMOUNT_VALUE));
        assertThat(INSTANCE.resolveToString(auditedFormattedAmountField, entity), is(Triptional.of(FORMATTED_AMOUNT_STRING_VALUE)));
    }

    @Test
    public void resolveToStringWhenNonNullAndDoesntHaveFormatter() {
        when(entity.safeGet(AMOUNT)).thenReturn(Triptional.of(AMOUNT_VALUE));
        assertThat(INSTANCE.resolveToString(auditedAmountField, entity), is(Triptional.of(NON_FORMATTED_AMOUNT_STRING_VALUE)));
    }

    @Test
    public void resolveToStringWhenNull() {
        when(entity.safeGet(AMOUNT)).thenReturn(Triptional.nullInstance());
        assertThat(INSTANCE.resolveToString(auditedAmountField, entity), is(Triptional.nullInstance()));
    }

    @Test
    public void resolveToStringWhenAbsent() {
        when(entity.safeGet(AMOUNT)).thenReturn(Triptional.absent());
        assertThat(INSTANCE.resolveToString(auditedAmountField, entity), is(Triptional.absent()));
    }
}