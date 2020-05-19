package com.kenshoo.pl.entity.internal.audit;

import com.google.common.collect.ImmutableSet;
import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.AbstractEntityType;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.Audited;
import com.kenshoo.pl.entity.internal.audit.entitytypes.*;
import org.junit.Test;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAndIs;
import static org.junit.Assert.assertThat;

public class AuditedFieldsResolverTest {

    private static final AuditedFieldsResolver RESOLVER = AuditedFieldsResolver.INSTANCE;

    @Test
    public void resolve_WhenEntityTypeIsAudited_AndHasId_ShouldReturnIdAndDataFields() {
        final AuditedFieldSet<AuditedType> expectedFieldSet =
            AuditedFieldSet.builder(AuditedType.ID)
                           .withDataFields(ImmutableSet.of(AuditedType.NAME,
                                                           AuditedType.DESC,
                                                           AuditedType.DESC2))
                           .build();

        assertThat(RESOLVER.resolve(AuditedType.INSTANCE),
                   isPresentAndIs(expectedFieldSet));
    }

    @Test
    public void resolve_WhenEntityTypeIsNotAudited_AndHasId_AndAuditedFields_ShouldReturnIdAndAuditedFields() {
        final AuditedFieldSet<InclusiveAuditedType> expectedFieldSet =
            AuditedFieldSet.builder(InclusiveAuditedType.ID)
                           .withDataFields(ImmutableSet.of(InclusiveAuditedType.NAME,
                                                           InclusiveAuditedType.DESC))
                           .build();

        assertThat(RESOLVER.resolve(InclusiveAuditedType.INSTANCE),
                   isPresentAndIs(expectedFieldSet));
    }

    @Test
    public void resolve_WhenEntityTypeIsAudited_AndHasId_AndHasNotAuditedFields_ShouldReturnIdAndOtherFields() {
        final AuditedFieldSet<ExclusiveAuditedType> expectedFieldSet =
            AuditedFieldSet.builder(ExclusiveAuditedType.ID)
                           .withDataFields(ImmutableSet.of(ExclusiveAuditedType.NAME))
                           .build();

        assertThat(RESOLVER.resolve(ExclusiveAuditedType.INSTANCE),
                   isPresentAndIs(expectedFieldSet));
    }

    @Test
    public void resolve_WhenEntityTypeIsAudited_AndHasNoId_ShouldReturnEmpty() {
        assertThat(RESOLVER.resolve(TestAuditedEntityWithoutIdType.INSTANCE), isEmpty());
    }

    @Test
    public void resolve_WhenEntityTypeIsNotAudited_AndHasId_ShouldReturnEmpty() {
        assertThat(RESOLVER.resolve(NotAuditedType.INSTANCE), isEmpty());
    }

    @Test
    public void resolve_WhenEntityTypeIsNotAudited_AndHasNoId_ShouldReturnEmpty() {
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