package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.pl.entity.internal.audit.entitytypes.*;
import org.junit.Test;

import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;

public class ExternalMandatoryFieldsExtractorTest {

    private static final ExternalMandatoryFieldsExtractor EXTRACTOR = ExternalMandatoryFieldsExtractor.INSTANCE;

    @Test
    public void resolve_WhenAudited_AndHasExternalMandatory_ShouldReturnThem() {
        final Set<AuditedField<?, ?>> expectedAuditedFields = Stream.of(NotAuditedAncestorType.NAME,
                                                                        NotAuditedAncestorType.DESC)
                                                                    .map(AuditedField::new)
                                                                    .collect(toUnmodifiableSet());

        assertThat(EXTRACTOR.extract(AuditedWithAncestorMandatoryType.INSTANCE).collect(toUnmodifiableSet()),
                   equalTo(expectedAuditedFields));
    }

    @Test
    public void resolve_WhenAudited_AndHasNoExtensions_ShouldReturnEmpty() {
        assertThat(EXTRACTOR.extract(AuditedType.INSTANCE).collect(toSet()), is(empty()));
    }

    @Test
    public void resolve_WhenAudited_AndHasInvalidExtensions_ShouldReturnEmpty() {
        assertThat(EXTRACTOR.extract(AuditedWithInvalidExtensionsType.INSTANCE).collect(toSet()), is(empty()));
    }

    @Test
    public void resolve_WhenNotAudited_ShouldReturnEmpty() {
        assertThat(EXTRACTOR.extract(NotAuditedType.INSTANCE).collect(toSet()), is(empty()));
    }
}