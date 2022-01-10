package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.pl.entity.internal.audit.ancestorfieldsproviders.AncestorWithFieldNameOverridesAuditExtensions;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithAncestorFieldNameOverridesType;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithInvalidExtensionsType;
import com.kenshoo.pl.entity.internal.audit.entitytypes.NotAuditedType;
import com.kenshoo.pl.entity.spi.audit.AuditExtensions.EmptyAuditExtensions;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

public class AuditExtensionsExtractorTest {

    private final AuditExtensionsExtractor extractor = new AuditExtensionsExtractor();

    @Test
    public void resolve_WhenAudited_AndHasExtensions_ShouldReturnCorrectExtensionsObj() {
        final var maybeExtensionsObj = extractor.extract(AuditedWithAncestorFieldNameOverridesType.INSTANCE);

        maybeExtensionsObj.ifPresentOrElse(
            extensionsObj -> assertThat("Incorrect type of extensions object returned: ",
                                       extensionsObj, Matchers.instanceOf(AncestorWithFieldNameOverridesAuditExtensions.class)),
            () -> fail("No extensions object was returned"));
    }

    @Test
    public void resolve_WhenAudited_AndHasNoExtensions_ShouldReturnEmptyExtensionObj() {
        final var maybeExtensionsObj = extractor.extract(AuditedType.INSTANCE);

        maybeExtensionsObj.ifPresentOrElse(
            extensionsObj -> assertThat("Incorrect type of extensions object returned: ",
                                        extensionsObj, Matchers.instanceOf(EmptyAuditExtensions.class)),
            () -> fail("No extensions object was returned"));
    }

    @Test
    public void resolve_WhenAudited_AndHasInvalidExtensions_ShouldReturnEmpty() {
        assertThat(extractor.extract(AuditedWithInvalidExtensionsType.INSTANCE), is(Optional.empty()));
    }

    @Test
    public void resolve_WhenNotAudited_ShouldReturnEmpty() {
        assertThat(extractor.extract(NotAuditedType.INSTANCE), is(Optional.empty()));
    }
}