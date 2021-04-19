package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.pl.entity.internal.audit.entitytypes.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Set;

import static com.kenshoo.pl.entity.internal.audit.ancestorfieldsproviders.AncestorWithFieldNameOverridesAuditExtensions.DESC_FIELD_NAME_OVERRIDE;
import static com.kenshoo.pl.entity.internal.audit.ancestorfieldsproviders.AncestorWithFieldNameOverridesAuditExtensions.NAME_FIELD_NAME_OVERRIDE;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;

@RunWith(MockitoJUnitRunner.class)
public class ExternalMandatoryFieldsExtractorTest {

    @InjectMocks
    private ExternalMandatoryFieldsExtractor extractor;

    @Test
    public void resolve_WhenAudited_AndHasExternalMandatory_WithNameOverrides_ShouldReturnThem() {
        final Set<AuditedField<?, ?>> expectedAuditedFields =
            Set.of(AuditedField.builder(NotAuditedAncestorType.NAME).withName(NAME_FIELD_NAME_OVERRIDE).build(),
                   AuditedField.builder(NotAuditedAncestorType.DESC).withName(DESC_FIELD_NAME_OVERRIDE).build());

        assertThat(extractor.extract(AuditedWithAncestorFieldNameOverridesType.INSTANCE).collect(toUnmodifiableSet()),
                   equalTo(expectedAuditedFields));
    }

    @Test
    public void resolve_WhenAudited_AndHasExternalMandatory_WithDefaultNames_ShouldReturnThem() {
        final Set<AuditedField<?, ?>> expectedAuditedFields =
            Set.of(AuditedField.builder(NotAuditedAncestorType.NAME).build(),
                   AuditedField.builder(NotAuditedAncestorType.DESC).build(),
                   AuditedField.builder(NotAuditedAncestorType.AMOUNT).build(),
                   AuditedField.builder(NotAuditedAncestorType.AMOUNT2).build());

        assertThat(extractor.extract(AuditedWithAncestorMandatoryType.INSTANCE).collect(toUnmodifiableSet()),
                   equalTo(expectedAuditedFields));
    }

    @Test
    public void resolve_WhenAudited_AndHasNoExtensions_ShouldReturnEmpty() {
        assertThat(extractor.extract(AuditedType.INSTANCE).collect(toSet()), is(empty()));
    }

    @Test
    public void resolve_WhenAudited_AndHasInvalidExtensions_ShouldReturnEmpty() {
        assertThat(extractor.extract(AuditedWithInvalidExtensionsType.INSTANCE).collect(toSet()), is(empty()));
    }

    @Test
    public void resolve_WhenNotAudited_ShouldReturnEmpty() {
        assertThat(extractor.extract(NotAuditedType.INSTANCE).collect(toSet()), is(empty()));
    }
}