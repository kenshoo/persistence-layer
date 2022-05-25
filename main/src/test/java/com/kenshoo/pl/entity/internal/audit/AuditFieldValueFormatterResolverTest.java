package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.pl.entity.internal.audit.entitytypes.*;
import com.kenshoo.pl.entity.internal.audit.formatters.CustomAuditFieldValueFormatter1;
import com.kenshoo.pl.entity.internal.audit.formatters.CustomAuditFieldValueFormatter2;
import com.kenshoo.pl.entity.spi.audit.DefaultAuditFieldValueFormatter;
import org.junit.Test;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

public class AuditFieldValueFormatterResolverTest {

    private final AuditFieldValueFormatterResolver formatterResolver = AuditFieldValueFormatterResolver.INSTANCE;

    @Test
    public void shouldReturnFieldLevelFormatterWhenEntityAuditedWithoutFormatterAndFieldHasFormatter() {
        final var formatter = formatterResolver.resolve(AuditedWithFieldLevelOnlyValueFormatterType.NAME);
        assertThat(formatter, instanceOf(CustomAuditFieldValueFormatter1.class));
    }

    @Test
    public void shouldReturnEntityLevelFormatterWhenEntityHasFormatterAndFieldAuditedWithoutFormatter() {
        final var formatter = formatterResolver.resolve(AuditedWithEntityLevelValueFormatterType.NAME);
        assertThat(formatter, instanceOf(CustomAuditFieldValueFormatter1.class));
    }

    @Test
    public void shouldReturnFieldLevelFormatterWhenEntityHasFormatterAndFieldHasDifferentOne() {
        final var formatter = formatterResolver.resolve(AuditedWithEntityLevelValueFormatterType.DESC);
        assertThat(formatter, instanceOf(CustomAuditFieldValueFormatter2.class));
    }

    @Test
    public void shouldReturnEntityLevelFormatterWhenEntityHasFormatterAndFieldHasNoAnnotations() {
        final var formatter = formatterResolver.resolve(AuditedWithEntityLevelValueFormatterType.DESC2);
        assertThat(formatter, instanceOf(CustomAuditFieldValueFormatter1.class));
    }

    @Test
    public void shouldReturnDefaultFormatterWhenEntityAuditedWithoutFormatterAndFieldHasNoAnnotations() {
        final var formatter = formatterResolver.resolve(AuditedAutoIncIdType.DESC);
        assertThat(formatter, instanceOf(DefaultAuditFieldValueFormatter.class));
    }

    @Test
    public void shouldReturnDefaultFormatterWhenEntityNotAuditedAndFieldAuditedWithoutFormatter() {
        final var formatter = formatterResolver.resolve(InclusiveAuditedType.NAME);
        assertThat(formatter, instanceOf(DefaultAuditFieldValueFormatter.class));
    }

    @Test
    public void shouldReturnDefaultFormatterWhenEntityAndFieldBothAuditedWithoutFormatter() {
        final var formatter = formatterResolver.resolve(AuditedWithAllVariationsType.NAME);
        assertThat(formatter, instanceOf(DefaultAuditFieldValueFormatter.class));
    }

    @Test
    public void shouldReturnDefaultFormatterWhenFieldHasInvalidFormatter() {
        final var formatter = formatterResolver.resolve(AuditedWithInvalidFieldLevelValueFormatterType.NAME);
        assertThat(formatter, instanceOf(DefaultAuditFieldValueFormatter.class));
    }

    @Test
    public void shouldReturnDefaultFormatterWhenEntityHasInvalidFormatterAndFieldHasNoAnnotations() {
        final var formatter = formatterResolver.resolve(AuditedWithInvalidEntityLevelValueFormatterType.NAME);
        assertThat(formatter, instanceOf(DefaultAuditFieldValueFormatter.class));
    }
}