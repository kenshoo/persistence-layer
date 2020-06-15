package com.kenshoo.pl.entity.internal.audit;

import com.google.common.collect.ImmutableSet;
import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.AbstractEntityType;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.internal.audit.entitytypes.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.stream.Stream;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAndIs;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuditedFieldsResolverTest {

    @Mock
    private ExternalMandatoryFieldsExtractor externalMandatoryFieldsExtractor;

    @InjectMocks
    private AuditedFieldsResolver fieldsResolver;

    @Before
    public void setUp() {
    }

    @Test
    public void resolve_WhenAudited_AndHasId_AndOtherFields_ShouldReturnIdAndOnChangeFields() {
        when(externalMandatoryFieldsExtractor.extract(AuditedType.INSTANCE)).thenReturn(Stream.empty());

        final AuditedFieldSet<AuditedType> expectedFieldSet =
            AuditedFieldSet.builder(AuditedType.ID)
                           .withOnChangeFields(ImmutableSet.of(AuditedType.NAME,
                                                               AuditedType.DESC,
                                                               AuditedType.DESC2))
                           .build();

        assertThat(fieldsResolver.resolve(AuditedType.INSTANCE),
                   isPresentAndIs(expectedFieldSet));
    }

    @Test
    public void resolve_WhenAudited_AndHasId_AndSelfMandatoryFields_ShouldReturnIdAndSelfMandatoryFields() {
        when(externalMandatoryFieldsExtractor.extract(AuditedWithSelfMandatoryOnlyType.INSTANCE)).thenReturn(Stream.empty());

        final AuditedFieldSet<AuditedWithSelfMandatoryOnlyType> expectedFieldSet =
            AuditedFieldSet.builder(AuditedWithSelfMandatoryOnlyType.ID)
                           .withSelfMandatoryFields(AuditedWithSelfMandatoryOnlyType.NAME)
                           .build();

        assertThat(fieldsResolver.resolve(AuditedWithSelfMandatoryOnlyType.INSTANCE),
                   isPresentAndIs(expectedFieldSet));
    }

    @Test
    public void resolve_WhenAudited_AndHasId_AndExternalMandatoryFields_AndOtherFields_ShouldReturnIdAndExternalMandatoryAndOnChange() {
        doReturn(Stream.of(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC))
            .when(externalMandatoryFieldsExtractor).extract(AuditedWithAncestorMandatoryType.INSTANCE);

        final AuditedFieldSet<AuditedWithAncestorMandatoryType> expectedFieldSet =
            AuditedFieldSet.builder(AuditedWithAncestorMandatoryType.ID)
                           .withOnChangeFields(ImmutableSet.of(AuditedWithAncestorMandatoryType.NAME,
                                                               AuditedWithAncestorMandatoryType.DESC,
                                                               AuditedWithAncestorMandatoryType.DESC2))
                           .withExternalMandatoryFields(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC)
                           .build();

        assertThat(fieldsResolver.resolve(AuditedWithAncestorMandatoryType.INSTANCE),
                   isPresentAndIs(expectedFieldSet));
    }

    @Test
    public void resolve_WhenAudited_AndHasEverything_ShouldReturnEverything() {
        doReturn(Stream.of(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC))
            .when(externalMandatoryFieldsExtractor).extract(AuditedWithSelfAndAncestorMandatoryType.INSTANCE);

        final AuditedFieldSet<AuditedWithSelfAndAncestorMandatoryType> expectedFieldSet =
            AuditedFieldSet.builder(AuditedWithSelfAndAncestorMandatoryType.ID)
                           .withExternalMandatoryFields(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC)
                           .withSelfMandatoryFields(AuditedWithSelfAndAncestorMandatoryType.NAME)
                           .withOnChangeFields(ImmutableSet.of(AuditedWithSelfAndAncestorMandatoryType.DESC,
                                                               AuditedWithSelfAndAncestorMandatoryType.DESC2))
                           .build();

        assertThat(fieldsResolver.resolve(AuditedWithSelfAndAncestorMandatoryType.INSTANCE),
                   isPresentAndIs(expectedFieldSet));
    }

    @Test
    public void resolve_WhenInclusiveAudited_AndHasId_ShouldReturnIdAndOnChangeForIncludedFields() {
        when(externalMandatoryFieldsExtractor.extract(InclusiveAuditedType.INSTANCE)).thenReturn(Stream.empty());

        final AuditedFieldSet<InclusiveAuditedType> expectedFieldSet =
            AuditedFieldSet.builder(InclusiveAuditedType.ID)
                           .withOnChangeFields(ImmutableSet.of(InclusiveAuditedType.NAME,
                                                               InclusiveAuditedType.DESC))
                           .build();

        assertThat(fieldsResolver.resolve(InclusiveAuditedType.INSTANCE),
                   isPresentAndIs(expectedFieldSet));
    }

    @Test
    public void resolve_WhenExclusiveAudited_AndHasId_ShouldReturnIdAndOnChangeForNotExcludedFields() {
        when(externalMandatoryFieldsExtractor.extract(ExclusiveAuditedType.INSTANCE)).thenReturn(Stream.empty());

        final AuditedFieldSet<ExclusiveAuditedType> expectedFieldSet =
            AuditedFieldSet.builder(ExclusiveAuditedType.ID)
                           .withOnChangeFields(ImmutableSet.of(ExclusiveAuditedType.NAME))
                           .build();

        assertThat(fieldsResolver.resolve(ExclusiveAuditedType.INSTANCE),
                   isPresentAndIs(expectedFieldSet));
    }

    @Test
    public void resolve_WhenAudited_AndHasNoId_ShouldReturnEmpty() {
        assertThat(fieldsResolver.resolve(TestAuditedEntityWithoutIdType.INSTANCE), isEmpty());
    }

    @Test
    public void resolve_WhenNotAudited_AndHasId_ShouldReturnEmpty() {
        assertThat(fieldsResolver.resolve(NotAuditedType.INSTANCE), isEmpty());
    }

    @Test
    public void resolve_WhenNotAudited_AndHasNoId_ShouldReturnEmpty() {
        assertThat(fieldsResolver.resolve(TestEntityWithoutIdType.INSTANCE), isEmpty());
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