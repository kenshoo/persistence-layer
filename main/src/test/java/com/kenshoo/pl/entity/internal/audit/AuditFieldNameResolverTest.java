package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithFieldNameOverridesType;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class AuditFieldNameResolverTest {

    @Test
    public void resolveWhenHasNonBlankNameAnnotationShouldReturnIt() {
        final var resolvedName = AuditFieldNameResolver.INSTANCE.resolve(AuditedWithFieldNameOverridesType.DESC);
        assertThat(resolvedName, is(AuditedWithFieldNameOverridesType.DESC_FIELD_NAME_OVERRIDE));
    }

    @Test
    public void resolveWhenHasBlankNameAnnotationShouldReturnDefaultFieldName() {
        final var resolvedName = AuditFieldNameResolver.INSTANCE.resolve(AuditedWithFieldNameOverridesType.DESC2);
        assertThat(resolvedName, is(AuditedWithFieldNameOverridesType.DESC2.toString()));
    }

    @Test
    public void resolveWhenHasEmptyNameAnnotationShouldReturnDefaultFieldName() {
        final var resolvedName = AuditFieldNameResolver.INSTANCE.resolve(AuditedWithFieldNameOverridesType.AMOUNT);
        assertThat(resolvedName, is(AuditedWithFieldNameOverridesType.AMOUNT.toString()));
    }

    @Test
    public void resolveWhenHasNoAnnotationShouldReturnDefaultFieldName() {
        final var resolvedName = AuditFieldNameResolver.INSTANCE.resolve(AuditedWithFieldNameOverridesType.NAME);
        assertThat(resolvedName, is(AuditedWithFieldNameOverridesType.NAME.toString()));
    }
}