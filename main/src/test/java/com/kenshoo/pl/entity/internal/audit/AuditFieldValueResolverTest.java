package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.pl.entity.Entity;
import com.kenshoo.pl.entity.Triptional;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType;
import com.kenshoo.pl.entity.spi.audit.AuditFieldValueFormatter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType.AMOUNT;
import static com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType.DESC;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuditFieldValueResolverTest {

    private static final String DESC_VALUE = "A description";

    private static final double AMOUNT_VALUE = 12.1199999;
    private static final String FORMATTED_AMOUNT_VALUE = "12.12";

    private final AuditedField<AuditedType, String> auditedDescField = AuditedField.builder(DESC).build();
    private final AuditedField<AuditedType, Double> auditedAmountField = AuditedField.builder(AMOUNT).build();

    @Mock
    private AuditFieldValueFormatter fieldValueFormatter;

    @Mock
    private Entity entity;

    @InjectMocks
    private AuditFieldValueResolver fieldValueResolver;
    
    @Test
    public void resolveWhenNonNull() {
        when(entity.safeGet(DESC)).thenReturn(Triptional.of(DESC_VALUE));
        assertThat(fieldValueResolver.resolve(auditedDescField, entity), is(Triptional.of(DESC_VALUE)));
    }

    @Test
    public void resolveWhenNull() {
        when(entity.safeGet(DESC)).thenReturn(Triptional.nullInstance());
        assertThat(fieldValueResolver.resolve(auditedDescField, entity), is(Triptional.nullInstance()));
    }

    @Test
    public void resolveWhenAbsent() {
        when(entity.safeGet(DESC)).thenReturn(Triptional.absent());
        assertThat(fieldValueResolver.resolve(auditedDescField, entity), is(Triptional.absent()));
    }

    @Test
    public void resolveToStringWhenNonNull() {
        when(entity.safeGet(AMOUNT)).thenReturn(Triptional.of(AMOUNT_VALUE));
        when(fieldValueFormatter.format(auditedAmountField, AMOUNT_VALUE)).thenReturn(FORMATTED_AMOUNT_VALUE);
        assertThat(fieldValueResolver.resolveToString(auditedAmountField, entity), is(Triptional.of(FORMATTED_AMOUNT_VALUE)));
    }

    @Test
    public void resolveToStringWhenNull() {
        when(entity.safeGet(AMOUNT)).thenReturn(Triptional.nullInstance());
        assertThat(fieldValueResolver.resolveToString(auditedAmountField, entity), is(Triptional.nullInstance()));
    }

    @Test
    public void resolveToStringWhenAbsent() {
        when(entity.safeGet(AMOUNT)).thenReturn(Triptional.absent());
        assertThat(fieldValueResolver.resolveToString(auditedAmountField, entity), is(Triptional.absent()));
    }
}