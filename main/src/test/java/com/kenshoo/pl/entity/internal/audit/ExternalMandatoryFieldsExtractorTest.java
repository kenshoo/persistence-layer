package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.pl.entity.internal.audit.entitytypes.*;
import com.kenshoo.pl.entity.internal.audit.formatters.CustomAuditFieldValueFormatter1;
import com.kenshoo.pl.entity.internal.audit.formatters.CustomAuditFieldValueFormatter2;
import com.kenshoo.pl.entity.matchers.audit.AuditedFieldMatcher;
import com.kenshoo.pl.entity.spi.audit.AuditFieldValueFormatter;
import com.kenshoo.pl.entity.spi.audit.DefaultAuditFieldValueFormatter;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Set;
import java.util.stream.Stream;

import static com.kenshoo.pl.entity.internal.audit.ancestorfieldsproviders.AncestorWithFieldNameOverridesAuditExtensions.DESC_FIELD_NAME_OVERRIDE;
import static com.kenshoo.pl.entity.internal.audit.ancestorfieldsproviders.AncestorWithFieldNameOverridesAuditExtensions.NAME_FIELD_NAME_OVERRIDE;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExternalMandatoryFieldsExtractorTest {

    @Mock
    private AuditFieldValueFormatterResolver valueFormatterResolver;

    @InjectMocks
    private ExternalMandatoryFieldsExtractor extractor;

    private final AuditFieldValueFormatter customValueFormatter1 = new CustomAuditFieldValueFormatter1();
    private final AuditFieldValueFormatter customValueFormatter2 = new CustomAuditFieldValueFormatter2();

    @Test
    public void resolve_WhenAudited_AndHasExternalMandatory_WithNameOverrides_ShouldReturnThem() {
        final Set<AuditedField<?, ?>> expectedAuditedFields =
            Set.of(AuditedField.builder(NotAuditedAncestorType.NAME).withName(NAME_FIELD_NAME_OVERRIDE).build(),
                   AuditedField.builder(NotAuditedAncestorType.DESC).withName(DESC_FIELD_NAME_OVERRIDE).build());

        Stream.of(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC)
              .forEach(field -> when(valueFormatterResolver.resolve(field)).thenReturn(DefaultAuditFieldValueFormatter.INSTANCE));

        assertThat(extractor.extract(AuditedWithAncestorFieldNameOverridesType.INSTANCE).collect(toUnmodifiableSet()),
                   equalTo(expectedAuditedFields));
    }

    @Test
    public void resolve_WhenAudited_AndHasExternalMandatory_WithValueFormatterOverrides_ShouldReturnThem() {
        final Set<AuditedField<?, ?>> expectedAuditedFields =
            Set.of(AuditedField.builder(NotAuditedAncestorType.NAME).withValueFormatter(customValueFormatter1).build(),
                   AuditedField.builder(NotAuditedAncestorType.DESC).withValueFormatter(customValueFormatter2).build());

        final Set<Matcher<? super AuditedField<?, ?>>> auditedFieldMatchers =
            expectedAuditedFields.stream()
                                 .map(AuditedFieldMatcher::eqAuditedField)
                                 .collect(toUnmodifiableSet());

        assertThat(extractor.extract(AuditedWithAncestorValueFormatterOverridesType.INSTANCE).collect(toUnmodifiableSet()),
                   containsInAnyOrder(auditedFieldMatchers));
    }

    @Test
    public void resolve_WhenAudited_AndHasExternalMandatory_WithDefaultNames_ShouldReturnThem() {
        final Set<AuditedField<?, ?>> expectedAuditedFields =
            Set.of(AuditedField.builder(NotAuditedAncestorType.NAME).build(),
                   AuditedField.builder(NotAuditedAncestorType.DESC).build(),
                   AuditedField.builder(NotAuditedAncestorType.AMOUNT).build(),
                   AuditedField.builder(NotAuditedAncestorType.AMOUNT2).build());

        Stream.of(NotAuditedAncestorType.NAME,
                  NotAuditedAncestorType.DESC,
                  NotAuditedAncestorType.AMOUNT,
                  NotAuditedAncestorType.AMOUNT2)
              .forEach(field -> when(valueFormatterResolver.resolve(field)).thenReturn(DefaultAuditFieldValueFormatter.INSTANCE));

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