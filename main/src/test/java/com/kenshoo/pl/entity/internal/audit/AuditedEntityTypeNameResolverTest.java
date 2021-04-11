package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithBlankNameOverrideType;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithNameOverrideType;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class AuditedEntityTypeNameResolverTest {

    private static final AuditedEntityTypeNameResolver RESOLVER = AuditedEntityTypeNameResolver.INSTANCE;

    @Test
    public void resolveWhenAnnotatedIsNotBlankShouldReturnOverride() {
        assertThat(RESOLVER.resolve(AuditedWithNameOverrideType.INSTANCE),
                   is(AuditedWithNameOverrideType.NAME_OVERRIDE));
    }

    @Test
    public void resolveWhenAnnotatedIsBlankShouldReturnDefault() {
        assertThat(RESOLVER.resolve(AuditedWithBlankNameOverrideType.INSTANCE),
                   is(AuditedWithBlankNameOverrideType.INSTANCE.getName()));
    }
}