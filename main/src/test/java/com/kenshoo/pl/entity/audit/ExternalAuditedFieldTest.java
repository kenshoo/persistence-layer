package com.kenshoo.pl.entity.audit;

import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType;
import com.kenshoo.pl.entity.spi.audit.AuditFieldValueFormatter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ExternalAuditedFieldTest {

    @Mock
    private AuditFieldValueFormatter valueFormatter;

    @Test
    public void getValueFormatterShouldReturnTheFormatterWhenProvided() {
        final var externalAuditedField = new ExternalAuditedField.Builder<>(AuditedType.NAME)
            .withValueFormatter(valueFormatter)
            .build();

        assertThat(externalAuditedField.getValueFormatter(), is(Optional.of(valueFormatter)));
    }

    @Test
    public void getValueFormatterShouldReturnEmptyWhenNoFormatterProvided() {
        final var externalAuditedField = new ExternalAuditedField.Builder<>(AuditedType.NAME).build();
        assertThat(externalAuditedField.getValueFormatter(), is(Optional.empty()));
    }

}