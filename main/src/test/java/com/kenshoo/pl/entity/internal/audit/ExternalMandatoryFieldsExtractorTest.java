package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.pl.entity.internal.audit.ancestorfieldsproviders.AncestorAuditExtensions;
import com.kenshoo.pl.entity.internal.audit.ancestorfieldsproviders.AncestorWithFieldNameOverridesAuditExtensions;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType;
import com.kenshoo.pl.entity.internal.audit.entitytypes.NotAuditedAncestorType;
import com.kenshoo.pl.entity.spi.audit.AuditExtensions.EmptyAuditExtensions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;
import java.util.Set;

import static com.kenshoo.pl.entity.internal.audit.ancestorfieldsproviders.AncestorWithFieldNameOverridesAuditExtensions.DESC_FIELD_NAME_OVERRIDE;
import static com.kenshoo.pl.entity.internal.audit.ancestorfieldsproviders.AncestorWithFieldNameOverridesAuditExtensions.NAME_FIELD_NAME_OVERRIDE;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.mockito.Mockito.doReturn;

@RunWith(MockitoJUnitRunner.class)
public class ExternalMandatoryFieldsExtractorTest {

    @Mock
    private AuditExtensionsExtractor auditExtensionsExtractor;

    @InjectMocks
    private ExternalMandatoryFieldsExtractor extractor;

    @Test
    public void resolve_WhenHasMandatoryFields_AndNameOverrides_ShouldReturnThem() {
        final Set<AuditedField<?, ?>> expectedAuditedFields =
            Set.of(AuditedField.builder(NotAuditedAncestorType.NAME).withName(NAME_FIELD_NAME_OVERRIDE).build(),
                   AuditedField.builder(NotAuditedAncestorType.DESC).withName(DESC_FIELD_NAME_OVERRIDE).build());

        doReturn(Optional.of(new AncestorWithFieldNameOverridesAuditExtensions())).when(auditExtensionsExtractor).extract(AuditedType.INSTANCE);

        assertThat(extractor.extract(AuditedType.INSTANCE).collect(toUnmodifiableSet()),
                   equalTo(expectedAuditedFields));
    }

    @Test
    public void resolve_WhenHasMandatoryFields_AndDefaultNames_ShouldReturnThem() {
        final Set<AuditedField<?, ?>> expectedAuditedFields =
            Set.of(AuditedField.builder(NotAuditedAncestorType.NAME).build(),
                   AuditedField.builder(NotAuditedAncestorType.DESC).build(),
                   AuditedField.builder(NotAuditedAncestorType.AMOUNT).build(),
                   AuditedField.builder(NotAuditedAncestorType.AMOUNT2).build());

        doReturn(Optional.of(new AncestorAuditExtensions())).when(auditExtensionsExtractor).extract(AuditedType.INSTANCE);

        assertThat(extractor.extract(AuditedType.INSTANCE).collect(toUnmodifiableSet()),
                   equalTo(expectedAuditedFields));
    }

    @Test
    public void resolve_WhenHasNoMandatoryFields_ShouldReturnEmpty() {
        doReturn(Optional.of(new EmptyAuditExtensions())).when(auditExtensionsExtractor).extract(AuditedType.INSTANCE);

        assertThat(extractor.extract(AuditedType.INSTANCE).collect(toSet()), is(empty()));
    }

    @Test
    public void resolve_WhenHasNoExtensions_ShouldReturnEmpty() {
        doReturn(Optional.empty()).when(auditExtensionsExtractor).extract(AuditedType.INSTANCE);

        assertThat(extractor.extract(AuditedType.INSTANCE).collect(toSet()), is(empty()));
    }
}