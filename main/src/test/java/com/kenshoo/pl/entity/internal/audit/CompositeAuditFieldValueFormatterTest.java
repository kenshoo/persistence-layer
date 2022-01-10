package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType;
import com.kenshoo.pl.entity.spi.audit.AuditExtensions;
import com.kenshoo.pl.entity.spi.audit.AuditFieldValueFormatter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType.DESC;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CompositeAuditFieldValueFormatterTest {

    private static final String DESC_VALUE = "A description";
    private static final String FORMATTED_DESC_VALUE = "A formatted description";

    private final AuditedField<AuditedType, String> auditedDescField = AuditedField.builder(DESC).build();

    @Mock
    private AuditExtensionsExtractor auditExtensionsExtractor;

    @Mock
    private AuditFieldValueFormatter defaultFormatter;

    @Mock
    private AuditFieldValueFormatter customFormatter;

    @Mock
    private AuditExtensions auditExtensions;

    private CompositeAuditFieldValueFormatter compositeFormatter;

    @Before
    public void setUp() {
        compositeFormatter = new CompositeAuditFieldValueFormatter(auditExtensionsExtractor, defaultFormatter);
    }

    @Test
    public void formatWhenAuditExtensionsExistShouldApplyCustomFormatter() {
        doReturn(Optional.of(auditExtensions)).when(auditExtensionsExtractor).extract(AuditedType.INSTANCE);
        when(auditExtensions.fieldValueFormatter()).thenReturn(customFormatter);
        when(customFormatter.format(auditedDescField, DESC_VALUE)).thenReturn(FORMATTED_DESC_VALUE);

        assertThat(compositeFormatter.format(auditedDescField, DESC_VALUE), is(FORMATTED_DESC_VALUE));

        verifyNoMoreInteractions(defaultFormatter);
    }

    @Test
    public void formatWhenNoAuditExtensionsExistShouldApplyDefaultFormatter() {
        doReturn(Optional.empty()).when(auditExtensionsExtractor).extract(AuditedType.INSTANCE);
        when(defaultFormatter.format(auditedDescField, DESC_VALUE)).thenReturn(FORMATTED_DESC_VALUE);

        assertThat(compositeFormatter.format(auditedDescField, DESC_VALUE), is(FORMATTED_DESC_VALUE));

        verifyNoMoreInteractions(customFormatter);
    }
}