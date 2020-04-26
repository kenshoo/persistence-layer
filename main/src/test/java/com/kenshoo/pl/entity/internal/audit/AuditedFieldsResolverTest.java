package com.kenshoo.pl.entity.internal.audit;

import com.google.common.collect.ImmutableSet;
import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.AbstractEntityType;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.Audited;
import org.junit.Test;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAndIs;
import static org.junit.Assert.assertThat;

public class AuditedFieldsResolverTest {

    private static final AuditedFieldsResolver RESOLVER = AuditedFieldsResolver.INSTANCE;

    @Test
    public void resolve_WhenEntityTypeIsAudited_AndHasId_ShouldReturnAllFields() {
        final AuditedFieldSet<TestAuditedEntityType> expectedFieldSet =
            new AuditedFieldSet<>(TestAuditedEntityType.ID,
                                  ImmutableSet.of(TestAuditedEntityType.NAME,
                                                  TestAuditedEntityType.DESC,
                                                  TestAuditedEntityType.DESC2));

        assertThat(RESOLVER.resolve(TestAuditedEntityType.INSTANCE),
                   isPresentAndIs(expectedFieldSet));
    }

    @Test
    public void resolve_WhenEntityTypeIsNotAudited_AndHasId_AndAuditedFields_ShouldReturnIdAndAuditedFields() {
        final AuditedFieldSet<TestEntityWithAuditedFieldsType> expectedFieldSet =
            new AuditedFieldSet<>(TestEntityWithAuditedFieldsType.ID,
                                  ImmutableSet.of(TestEntityWithAuditedFieldsType.NAME,
                                                  TestEntityWithAuditedFieldsType.DESC));

        assertThat(RESOLVER.resolve(TestEntityWithAuditedFieldsType.INSTANCE),
                   isPresentAndIs(expectedFieldSet));
    }

    @Test
    public void resolve_WhenEntityTypeIsAudited_AndHasId_AndHasNotAuditedFields_ShouldReturnIdAndOtherFields() {
        final AuditedFieldSet<TestAuditedEntityWithNotAuditedFieldsType> expectedFieldSet =
            new AuditedFieldSet<>(TestAuditedEntityWithNotAuditedFieldsType.ID,
                                  ImmutableSet.of(TestAuditedEntityWithNotAuditedFieldsType.NAME));

        assertThat(RESOLVER.resolve(TestAuditedEntityWithNotAuditedFieldsType.INSTANCE),
                   isPresentAndIs(expectedFieldSet));
    }

    @Test
    public void resolve_WhenEntityTypeIsAudited_AndHasNoId_ShouldReturnEmpty() {
        assertThat(RESOLVER.resolve(TestAuditedEntityWithoutIdType.INSTANCE), isEmpty());
    }

    @Test
    public void resolve_WhenEntityTypeIsNotAudited_AndHasId_ShouldReturnEmpty() {
        assertThat(RESOLVER.resolve(TestEntityType.INSTANCE), isEmpty());
    }

    @Test
    public void resolve_WhenEntityTypeIsNotAudited_AndHasNoId_ShouldReturnEmpty() {
        assertThat(RESOLVER.resolve(TestEntityWithoutIdType.INSTANCE), isEmpty());
    }

    @Audited
    public static class TestAuditedEntityWithoutIdType extends AbstractTestEntityType<TestAuditedEntityWithoutIdType> {

        public static final TestAuditedEntityWithoutIdType INSTANCE = new TestAuditedEntityWithoutIdType();

        public static final EntityField<TestAuditedEntityWithoutIdType, String> NAME = INSTANCE.field(TestEntityTable.INSTANCE.name);

        private TestAuditedEntityWithoutIdType() {
            super("TestAuditedEntityWithoutId");
        }
    }

    public static class TestEntityWithoutIdType extends AbstractEntityType<TestEntityWithoutIdType> {

        public static final TestEntityWithoutIdType INSTANCE = new TestEntityWithoutIdType();

        public static final EntityField<TestEntityWithoutIdType, String> NAME = INSTANCE.field(TestEntityTable.INSTANCE.name);

        @Override
        public DataTable getPrimaryTable() {
            return TestEntityTable.INSTANCE;
        }

        private TestEntityWithoutIdType() {
            super("TestEntityWithoutId");
        }
    }
}