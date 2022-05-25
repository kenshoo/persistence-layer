package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.pl.entity.Entity;
import com.kenshoo.pl.entity.Triptional;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedAutoIncIdType;
import com.kenshoo.pl.entity.spi.audit.AuditFieldValueFormatter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.kenshoo.pl.entity.internal.audit.AuditFieldValueResolver.INSTANCE;
import static com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedAutoIncIdType.AMOUNT;
import static com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedAutoIncIdType.DESC;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuditFieldValueResolverTest {

    private static final String DESC_VALUE = "A description";

    private static final double AMOUNT_VALUE = 12.1199999;
    private static final String FORMATTED_AMOUNT_VALUE = "12.12";

    private AuditedField<AuditedAutoIncIdType, String> auditedDescField;
    private AuditedField<AuditedAutoIncIdType, Double> auditedAmountField;

    @Mock
    private AuditFieldValueFormatter valueFormatter;

    @Mock
    private Entity entity;

    @Before
    public void setUp() {
        auditedDescField = AuditedField.builder(DESC).build();

        auditedAmountField = AuditedField.builder(AMOUNT)
                                         .withValueFormatter(valueFormatter)
                                         .build();
    }

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
    public void resolveToStringWhenNonNull() {
        when(entity.safeGet(AMOUNT)).thenReturn(Triptional.of(AMOUNT_VALUE));
        when(valueFormatter.format(AMOUNT, AMOUNT_VALUE)).thenReturn(FORMATTED_AMOUNT_VALUE);
        assertThat(INSTANCE.resolveToString(auditedAmountField, entity), is(Triptional.of(FORMATTED_AMOUNT_VALUE)));
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