package com.kenshoo.pl.entity.internal.audit;

import com.google.common.collect.ImmutableSet;
import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.AbstractEntityType;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.internal.audit.entitytypes.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.stream.Stream;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAndIs;
import static com.kenshoo.pl.entity.audit.AuditTrigger.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuditedFieldsResolverTest {

    @Mock
    private ExternalMandatoryFieldsExtractor externalMandatoryFieldsExtractor;

    @InjectMocks
    private AuditedFieldsResolver fieldsResolver;

    @Test
    public void resolve_WhenAudited_AndHasId_AndOnCreateAndUpdate_ShouldReturnIdAndOnCreateOrUpdate() {
        when(externalMandatoryFieldsExtractor.extract(AuditedType.INSTANCE)).thenReturn(Stream.empty());

        final AuditedFieldSet<AuditedType> expectedFieldSet =
            AuditedFieldSet.builder(AuditedType.ID)
                           .withInternalFields(ON_CREATE_OR_UPDATE,
                                               AuditedType.NAME, AuditedType.DESC, AuditedType.DESC2)
                           .build();

        assertThat(fieldsResolver.resolve(AuditedType.INSTANCE),
                   isPresentAndIs(expectedFieldSet));
    }

    @Test
    public void resolve_WhenAudited_AndHasId_AndOnUpdate_ShouldReturnIdAndOnUpdate() {
        when(externalMandatoryFieldsExtractor.extract(AuditedWithOnUpdateType.INSTANCE)).thenReturn(Stream.empty());

        final AuditedFieldSet<AuditedWithOnUpdateType> expectedFieldSet =
            AuditedFieldSet.builder(AuditedWithOnUpdateType.ID)
                           .withInternalFields(ON_UPDATE,
                                               AuditedWithOnUpdateType.NAME,
                                               AuditedWithOnUpdateType.DESC,
                                               AuditedWithOnUpdateType.DESC2)
                           .build();

        assertThat(fieldsResolver.resolve(AuditedWithOnUpdateType.INSTANCE),
                   isPresentAndIs(expectedFieldSet));
    }

    @Test
    public void resolve_WhenAudited_AndHasId_AndInternalMandatoryFields_ShouldReturnIdAndInternalMandatoryFields() {
        when(externalMandatoryFieldsExtractor.extract(AuditedWithInternalMandatoryOnlyType.INSTANCE)).thenReturn(Stream.empty());

        final AuditedFieldSet<AuditedWithInternalMandatoryOnlyType> expectedFieldSet =
            AuditedFieldSet.builder(AuditedWithInternalMandatoryOnlyType.ID)
                           .withInternalFields(ALWAYS, AuditedWithInternalMandatoryOnlyType.NAME)
                           .build();

        assertThat(fieldsResolver.resolve(AuditedWithInternalMandatoryOnlyType.INSTANCE),
                   isPresentAndIs(expectedFieldSet));
    }

    @Test
    public void resolve_WhenAudited_AndHasId_AndExternalMandatoryFields_AndOtherFields_ShouldReturnIdAndExternalMandatoryAndOnChange() {
        doReturn(Stream.of(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC))
            .when(externalMandatoryFieldsExtractor).extract(AuditedWithAncestorMandatoryType.INSTANCE);

        final AuditedFieldSet<AuditedWithAncestorMandatoryType> expectedFieldSet =
            AuditedFieldSet.builder(AuditedWithAncestorMandatoryType.ID)
                           .withInternalFields(ON_CREATE_OR_UPDATE,
                                               AuditedWithAncestorMandatoryType.NAME,
                                               AuditedWithAncestorMandatoryType.DESC,
                                               AuditedWithAncestorMandatoryType.DESC2)
                           .withExternalFields(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC)
                           .build();

        assertThat(fieldsResolver.resolve(AuditedWithAncestorMandatoryType.INSTANCE),
                   isPresentAndIs(expectedFieldSet));
    }

    @Test
    public void resolve_WhenAudited_AndHasEverything_ShouldReturnEverything() {
        doReturn(Stream.of(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC))
            .when(externalMandatoryFieldsExtractor).extract(AuditedWithAllVariationsType.INSTANCE);

        final AuditedFieldSet<AuditedWithAllVariationsType> expectedFieldSet =
            AuditedFieldSet.builder(AuditedWithAllVariationsType.ID)
                           .withExternalFields(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC)
                           .withInternalFields(ALWAYS, AuditedWithAllVariationsType.NAME)
                           .withInternalFields(ON_CREATE_OR_UPDATE, AuditedWithAllVariationsType.DESC)
                           .withInternalFields(ON_UPDATE, AuditedWithAllVariationsType.DESC2)
                           .build();

        assertThat(fieldsResolver.resolve(AuditedWithAllVariationsType.INSTANCE),
                   isPresentAndIs(expectedFieldSet));
    }

    @Test
    public void resolve_WhenInclusiveAudited_AndHasId_ShouldReturnIdAndOnCreateOrUpdateForIncludedFields() {
        when(externalMandatoryFieldsExtractor.extract(InclusiveAuditedType.INSTANCE)).thenReturn(Stream.empty());

        final AuditedFieldSet<InclusiveAuditedType> expectedFieldSet =
            AuditedFieldSet.builder(InclusiveAuditedType.ID)
                           .withInternalFields(ON_CREATE_OR_UPDATE,
                                               InclusiveAuditedType.NAME, InclusiveAuditedType.DESC)
                           .build();

        assertThat(fieldsResolver.resolve(InclusiveAuditedType.INSTANCE),
                   isPresentAndIs(expectedFieldSet));
    }

    @Test
    public void resolve_WhenExclusiveAudited_AndHasId_ShouldReturnIdAndOnCreateOrUpdateForNotExcludedFields() {
        when(externalMandatoryFieldsExtractor.extract(ExclusiveAuditedType.INSTANCE)).thenReturn(Stream.empty());

        final AuditedFieldSet<ExclusiveAuditedType> expectedFieldSet =
            AuditedFieldSet.builder(ExclusiveAuditedType.ID)
                           .withInternalFields(ON_CREATE_OR_UPDATE, ImmutableSet.of(ExclusiveAuditedType.NAME))
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