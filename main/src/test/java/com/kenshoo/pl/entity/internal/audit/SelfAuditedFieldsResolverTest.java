package com.kenshoo.pl.entity.internal.audit;

import com.google.common.collect.ImmutableSet;
import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.AbstractEntityType;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.internal.audit.entitytypes.*;
import org.junit.Test;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAndIs;
import static org.junit.Assert.assertThat;

public class SelfAuditedFieldsResolverTest {

    private static final AuditedFieldsResolver RESOLVER = SelfAuditedFieldsResolver.INSTANCE;

    @Test
    public void resolve_WhenAudited_AndHasId_ShouldReturnIdAndOnChangeForAllFields() {
        final AuditedFieldSet<AuditedType> expectedFieldSet =
            AuditedFieldSet.builder(AuditedType.ID)
                           .withOnChangeFields(ImmutableSet.of(AuditedType.NAME,
                                                               AuditedType.DESC,
                                                               AuditedType.DESC2))
                           .build();

        assertThat(RESOLVER.resolve(AuditedType.INSTANCE),
                   isPresentAndIs(expectedFieldSet));
    }

    @Test
    public void resolve_WhenAudited_AndHasId_AndMandatoryFields_AndOtherFields_ShouldReturnEverything() {
        final AuditedFieldSet<AuditedWithSelfMandatoryType> expectedFieldSet =
            AuditedFieldSet.builder(AuditedWithSelfMandatoryType.ID)
                           .withSelfMandatoryFields(AuditedWithSelfMandatoryType.NAME)
                           .withOnChangeFields(ImmutableSet.of(AuditedWithSelfMandatoryType.DESC,
                                                               AuditedWithSelfMandatoryType.DESC2))
                           .build();

        assertThat(RESOLVER.resolve(AuditedWithSelfMandatoryType.INSTANCE),
                   isPresentAndIs(expectedFieldSet));
    }

    @Test
    public void resolve_WhenInclusiveAudited_AndHasId_ShouldReturnIdAndOnChangeForIncludedFields() {
        final AuditedFieldSet<InclusiveAuditedType> expectedFieldSet =
            AuditedFieldSet.builder(InclusiveAuditedType.ID)
                           .withOnChangeFields(ImmutableSet.of(InclusiveAuditedType.NAME,
                                                               InclusiveAuditedType.DESC))
                           .build();

        assertThat(RESOLVER.resolve(InclusiveAuditedType.INSTANCE),
                   isPresentAndIs(expectedFieldSet));
    }

    @Test
    public void resolve_WhenExclusiveAudited_AndHasId_ShouldReturnIdAndOnChangeForNotExcludedFields() {
        final AuditedFieldSet<ExclusiveAuditedType> expectedFieldSet =
            AuditedFieldSet.builder(ExclusiveAuditedType.ID)
                           .withOnChangeFields(ImmutableSet.of(ExclusiveAuditedType.NAME))
                           .build();

        assertThat(RESOLVER.resolve(ExclusiveAuditedType.INSTANCE),
                   isPresentAndIs(expectedFieldSet));
    }

    @Test
    public void resolve_WhenAudited_AndHasNoId_ShouldReturnEmpty() {
        assertThat(RESOLVER.resolve(TestAuditedEntityWithoutIdType.INSTANCE), isEmpty());
    }

    @Test
    public void resolve_WhenNotAudited_AndHasId_ShouldReturnEmpty() {
        assertThat(RESOLVER.resolve(NotAuditedType.INSTANCE), isEmpty());
    }

    @Test
    public void resolve_WhenNotAudited_AndHasNoId_ShouldReturnEmpty() {
        assertThat(RESOLVER.resolve(TestEntityWithoutIdType.INSTANCE), isEmpty());
    }

    @Audited
    public static class TestAuditedEntityWithoutIdType extends AbstractType<TestAuditedEntityWithoutIdType> {

        public static final TestAuditedEntityWithoutIdType INSTANCE = new TestAuditedEntityWithoutIdType();

        public static final EntityField<TestAuditedEntityWithoutIdType, String> NAME = INSTANCE.field(MainTable.INSTANCE.name);

        private TestAuditedEntityWithoutIdType() {
            super("TestAuditedEntityWithoutId");
        }
    }

    public static class TestEntityWithoutIdType extends AbstractEntityType<TestEntityWithoutIdType> {

        public static final TestEntityWithoutIdType INSTANCE = new TestEntityWithoutIdType();

        public static final EntityField<TestEntityWithoutIdType, String> NAME = INSTANCE.field(MainTable.INSTANCE.name);

        @Override
        public DataTable getPrimaryTable() {
            return MainTable.INSTANCE;
        }

        private TestEntityWithoutIdType() {
            super("TestEntityWithoutId");
        }
    }
}