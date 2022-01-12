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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuditFieldValueResolverFactoryTest {

    @Mock
    private AuditExtensionsExtractor auditExtensionsExtractor;

    @Mock
    private AuditFieldValueFormatter defaultFormatter;

    @Mock
    private AuditFieldValueFormatter customFormatter;

    @Mock
    private AuditExtensions auditExtensions;

    private AuditFieldValueResolverFactory fieldValueResolverFactory;

    @Before
    public void setUp() {
        fieldValueResolverFactory = new AuditFieldValueResolverFactory(auditExtensionsExtractor, defaultFormatter);
    }

    @Test
    public void resolveWhenAuditExtensionsExistShouldReturnCustomFormatter() {
        doReturn(Optional.of(auditExtensions)).when(auditExtensionsExtractor).extract(AuditedType.INSTANCE);
        when(auditExtensions.fieldValueFormatter()).thenReturn(customFormatter);

        final var fieldValueResolver = fieldValueResolverFactory.create(AuditedType.INSTANCE);

        assertThat("There must be a created field resolver", fieldValueResolver, notNullValue());
        assertThat("Incorrect formatter passed to the field resolver: ",
                   fieldValueResolver.getFieldValueFormatter(), is(customFormatter));
    }

    @Test
    public void formatWhenNoAuditExtensionsExistShouldReturnDefaultFormatter() {
        doReturn(Optional.empty()).when(auditExtensionsExtractor).extract(AuditedType.INSTANCE);

        final var fieldValueResolver = fieldValueResolverFactory.create(AuditedType.INSTANCE);

        assertThat("There must be a created field resolver", fieldValueResolver, notNullValue());
        assertThat("Incorrect formatter passed to the field resolver: ",
                   fieldValueResolver.getFieldValueFormatter(), is(defaultFormatter));
    }
}