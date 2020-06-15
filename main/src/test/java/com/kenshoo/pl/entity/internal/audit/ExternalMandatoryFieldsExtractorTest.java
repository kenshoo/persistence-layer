package com.kenshoo.pl.entity.internal.audit;

import com.google.common.collect.ImmutableSet;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.internal.audit.entitytypes.*;
import org.junit.Test;

import static java.util.stream.Collectors.toSet;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;

public class ExternalMandatoryFieldsExtractorTest {

    private static final ExternalMandatoryFieldsExtractor EXTRACTOR = ExternalMandatoryFieldsExtractor.INSTANCE;

    @Test
    public void resolve_WhenAudited_AndHasExternalMandatory_ShouldReturnThem() {
        assertThat(EXTRACTOR.extract(AuditedWithAncestorMandatoryType.INSTANCE).collect(toSet()),
                   equalTo(ImmutableSet.<EntityField<?, ?>>of(NotAuditedAncestorType.NAME,
                                                              NotAuditedAncestorType.DESC)));
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