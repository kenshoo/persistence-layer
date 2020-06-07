package com.kenshoo.pl.entity.internal.audit;

import com.google.common.collect.ImmutableSet;
import com.kenshoo.pl.entity.internal.audit.entitytypes.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAndIs;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;

@RunWith(MockitoJUnitRunner.class)
public class AllAuditedFieldsResolverTest {

    @Mock
    private AuditedFieldsResolver selfFieldsResolver;

    @InjectMocks
    private AllAuditedFieldsResolver allFieldsResolver;

    @Test
    public void resolve_WhenAudited_AndHasExternalMandatory_ShouldReturnExternalMandatoryAndOnChange() {

        final AuditedFieldSet<AuditedWithAncestorMandatoryType> initialFieldSet =
            AuditedFieldSet.builder(AuditedWithAncestorMandatoryType.ID)
                           .withOnChangeFields(ImmutableSet.of(AuditedWithAncestorMandatoryType.NAME,
                                                               AuditedWithAncestorMandatoryType.DESC))
                           .build();

        doReturn(Optional.of(initialFieldSet)).when(selfFieldsResolver).resolve(AuditedWithAncestorMandatoryType.INSTANCE);

        final AuditedFieldSet<AuditedWithAncestorMandatoryType> expectedFinalFieldSet =
            AuditedFieldSet.builder(AuditedWithAncestorMandatoryType.ID)
                           .withExternalMandatoryFields(ImmutableSet.of(NotAuditedAncestorType.NAME,
                                                                NotAuditedAncestorType.DESC))
                           .withOnChangeFields(ImmutableSet.of(AuditedWithAncestorMandatoryType.NAME,
                                                               AuditedWithAncestorMandatoryType.DESC))
                           .build();

        assertThat(allFieldsResolver.resolve(AuditedWithAncestorMandatoryType.INSTANCE),
                   isPresentAndIs(expectedFinalFieldSet));
    }

    @Test
    public void resolve_WhenAudited_AndHasSelfMandatory_ShouldReturnSelfMandatoryAndOnChange() {

        final AuditedFieldSet<AuditedWithSelfMandatoryType> initialFieldSet =
            AuditedFieldSet.builder(AuditedWithSelfMandatoryType.ID)
                           .withSelfMandatoryFields(AuditedWithSelfMandatoryType.NAME)
                           .withOnChangeFields(ImmutableSet.of(AuditedWithSelfMandatoryType.NAME,
                                                               AuditedWithSelfMandatoryType.DESC))
                           .build();

        doReturn(Optional.of(initialFieldSet)).when(selfFieldsResolver).resolve(AuditedWithSelfMandatoryType.INSTANCE);

        assertThat(allFieldsResolver.resolve(AuditedWithSelfMandatoryType.INSTANCE),
                   isPresentAndIs(initialFieldSet));
    }

    @Test
    public void resolve_WhenAudited_AndHasSelfAndExternalMandatory_ShouldReturnBothMandatoriesAndOnChange() {

        final AuditedFieldSet<AuditedWithSelfAndAncestorMandatoryType> initialFieldSet =
            AuditedFieldSet.builder(AuditedWithSelfAndAncestorMandatoryType.ID)
                           .withExternalMandatoryFields(NotAuditedAncestorType.NAME,
                                                        NotAuditedAncestorType.DESC)
                           .withSelfMandatoryFields(AuditedWithSelfAndAncestorMandatoryType.NAME)
                           .withOnChangeFields(ImmutableSet.of(AuditedWithSelfAndAncestorMandatoryType.NAME,
                                                               AuditedWithSelfAndAncestorMandatoryType.DESC))
                           .build();

        doReturn(Optional.of(initialFieldSet)).when(selfFieldsResolver).resolve(AuditedWithSelfAndAncestorMandatoryType.INSTANCE);

        assertThat(allFieldsResolver.resolve(AuditedWithSelfAndAncestorMandatoryType.INSTANCE),
                   isPresentAndIs(initialFieldSet));
    }

    @Test
    public void resolve_WhenAudited_AndHasNoMandatory_ShouldReturnOnChangeFields() {

        final AuditedFieldSet<AuditedType> initialFieldSet =
            AuditedFieldSet.builder(AuditedType.ID)
                           .withOnChangeFields(ImmutableSet.of(AuditedType.NAME, AuditedType.DESC))
                           .build();

        doReturn(Optional.of(initialFieldSet)).when(selfFieldsResolver).resolve(AuditedType.INSTANCE);

        assertThat(allFieldsResolver.resolve(AuditedType.INSTANCE), isPresentAndIs(initialFieldSet));
    }

    @Test
    public void resolve_WhenAudited_AndHasInvalidExtensions_ShouldReturnOnChangeFields() {
        final AuditedFieldSet<AuditedWithInvalidExtensionsType> initialFieldSet =
            AuditedFieldSet.builder(AuditedWithInvalidExtensionsType.ID)
                           .withOnChangeFields(ImmutableSet.of(AuditedWithInvalidExtensionsType.NAME,
                                                               AuditedWithInvalidExtensionsType.DESC))
                           .build();

        doReturn(Optional.of(initialFieldSet)).when(selfFieldsResolver).resolve(AuditedWithInvalidExtensionsType.INSTANCE);

        assertThat(allFieldsResolver.resolve(AuditedWithInvalidExtensionsType.INSTANCE),
                   isPresentAndIs(initialFieldSet));
    }

    @Test
    public void resolve_WhenNotAudited_ShouldReturnEmpty() {
        doReturn(Optional.empty()).when(selfFieldsResolver).resolve(NotAuditedType.INSTANCE);

        assertThat(allFieldsResolver.resolve(NotAuditedType.INSTANCE), isEmpty());
    }
}