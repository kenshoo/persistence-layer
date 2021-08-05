package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithAllVariationsType;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithVirtualType;
import com.kenshoo.pl.entity.internal.audit.entitytypes.InclusiveAuditedWithVirtualType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAndIs;
import static com.kenshoo.pl.entity.audit.AuditTrigger.*;
import static com.kenshoo.pl.entity.internal.audit.AuditIndicator.AUDITED;
import static com.kenshoo.pl.entity.internal.audit.AuditIndicator.NOT_AUDITED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuditedFieldResolverTest {

    private static final String FIELD_NAME = "someName";

    @Mock
    private AuditFieldNameResolver fieldNameResolver;

    @InjectMocks
    private AuditedFieldResolver fieldResolver;

    @Test
    public void resolveWhenEntityNotAuditedAndFieldTriggeredOnCreateAndUpdate() {
        when(fieldNameResolver.resolve(AuditedWithAllVariationsType.DESC3)).thenReturn(FIELD_NAME);

        final var expectedAuditedField = AuditedField.builder(AuditedWithAllVariationsType.DESC3)
                                                     .withName(FIELD_NAME)
                                                     .withTrigger(ON_CREATE_OR_UPDATE)
                                                     .build();
        final var maybeActualAuditedField = fieldResolver.resolve(AuditedWithAllVariationsType.DESC3, NOT_AUDITED);

        assertThat(maybeActualAuditedField, isPresentAndIs(expectedAuditedField));
    }

    @Test
    public void resolveWhenEntityNotAuditedAndFieldTriggeredOnUpdate() {
        when(fieldNameResolver.resolve(AuditedWithAllVariationsType.DESC2)).thenReturn(FIELD_NAME);

        final var expectedAuditedField = AuditedField.builder(AuditedWithAllVariationsType.DESC2)
                                                     .withName(FIELD_NAME)
                                                     .withTrigger(ON_UPDATE)
                                                     .build();
        final var maybeActualAuditedField = fieldResolver.resolve(AuditedWithAllVariationsType.DESC2, NOT_AUDITED);

        assertThat(maybeActualAuditedField, isPresentAndIs(expectedAuditedField));
    }

    @Test
    public void resolveWhenEntityNotAuditedAndFieldTriggeredAlways() {
        when(fieldNameResolver.resolve(AuditedWithAllVariationsType.NAME)).thenReturn(FIELD_NAME);

        final var expectedAuditedField = AuditedField.builder(AuditedWithAllVariationsType.NAME)
                                                     .withName(FIELD_NAME)
                                                     .withTrigger(ALWAYS)
                                                     .build();
        final var maybeActualAuditedField = fieldResolver.resolve(AuditedWithAllVariationsType.NAME, NOT_AUDITED);

        assertThat(maybeActualAuditedField, isPresentAndIs(expectedAuditedField));
    }

    @Test
    public void resolveWhenEntityNotAuditedAndFieldHasNoAnnotationShouldReturnEmpty() {
        final var maybeActualAuditedField = fieldResolver.resolve(AuditedWithAllVariationsType.DESC, NOT_AUDITED);

        assertThat(maybeActualAuditedField, isEmpty());
    }

    @Test
    public void resolveWhenEntityAuditedAndFieldTriggeredOnCreateAndUpdate() {
        when(fieldNameResolver.resolve(AuditedWithAllVariationsType.DESC3)).thenReturn(FIELD_NAME);

        final var expectedAuditedField = AuditedField.builder(AuditedWithAllVariationsType.DESC3)
                                                     .withName(FIELD_NAME)
                                                     .withTrigger(ON_CREATE_OR_UPDATE)
                                                     .build();
        final var maybeActualAuditedField = fieldResolver.resolve(AuditedWithAllVariationsType.DESC3, AUDITED);

        assertThat(maybeActualAuditedField, isPresentAndIs(expectedAuditedField));
    }

    @Test
    public void resolveWhenEntityAuditedAndFieldTriggeredOnUpdate() {
        when(fieldNameResolver.resolve(AuditedWithAllVariationsType.DESC2)).thenReturn(FIELD_NAME);

        final var expectedAuditedField = AuditedField.builder(AuditedWithAllVariationsType.DESC2)
                                                     .withName(FIELD_NAME)
                                                     .withTrigger(ON_UPDATE)
                                                     .build();
        final var maybeActualAuditedField = fieldResolver.resolve(AuditedWithAllVariationsType.DESC2, AUDITED);

        assertThat(maybeActualAuditedField, isPresentAndIs(expectedAuditedField));
    }

    @Test
    public void resolveWhenEntityAuditedAndFieldTriggeredAlways() {
        when(fieldNameResolver.resolve(AuditedWithAllVariationsType.NAME)).thenReturn(FIELD_NAME);

        final var expectedAuditedField = AuditedField.builder(AuditedWithAllVariationsType.NAME)
                                                     .withName(FIELD_NAME)
                                                     .withTrigger(ALWAYS)
                                                     .build();
        final var maybeActualAuditedField = fieldResolver.resolve(AuditedWithAllVariationsType.NAME, AUDITED);

        assertThat(maybeActualAuditedField, isPresentAndIs(expectedAuditedField));
    }

    @Test
    public void resolveWhenEntityAuditedAndFieldNotAuditedShouldReturnEmpty() {
        final var maybeActualAuditedField = fieldResolver.resolve(AuditedWithAllVariationsType.ANCESTOR_ID, AUDITED);

        assertThat(maybeActualAuditedField, isEmpty());
    }

    @Test
    public void resolveWhenEntityAuditedAndFieldHasNoAnnotationShouldReturnFieldTriggeredOnCreateAndUpdate() {
        when(fieldNameResolver.resolve(AuditedWithAllVariationsType.DESC)).thenReturn(FIELD_NAME);

        final var expectedAuditedField = AuditedField.builder(AuditedWithAllVariationsType.DESC)
                                                     .withName(FIELD_NAME)
                                                     .withTrigger(ON_CREATE_OR_UPDATE)
                                                     .build();
        final var maybeActualAuditedField = fieldResolver.resolve(AuditedWithAllVariationsType.DESC, AUDITED);

        assertThat(maybeActualAuditedField, isPresentAndIs(expectedAuditedField));
    }

    @Test
    public void resolveWhenEntityAuditedAndFieldIsVirtualShouldReturnEmpty() {
        final var maybeActualAuditedField = fieldResolver.resolve(AuditedWithVirtualType.VIRTUAL_DESC_1, AUDITED);

        assertThat(maybeActualAuditedField, isEmpty());
    }

    @Test
    public void resolveWhenEntityNotAuditedAndFieldIsVirtualAndAuditedShouldReturnEmpty() {
        final var maybeActualAuditedField = fieldResolver.resolve(InclusiveAuditedWithVirtualType.VIRTUAL_DESC, AUDITED);

        assertThat(maybeActualAuditedField, isEmpty());
    }
}