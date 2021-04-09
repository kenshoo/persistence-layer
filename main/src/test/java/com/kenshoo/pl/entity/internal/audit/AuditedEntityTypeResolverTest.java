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
public class AuditedEntityTypeResolverTest {

    @Mock
    private ExternalMandatoryFieldsExtractor externalMandatoryFieldsExtractor;

    @InjectMocks
    private AuditedEntityTypeResolver auditedEntityTypeResolver;

    @Test
    public void resolve_WhenAudited_AndHasId_AndOnCreateAndUpdate_ShouldReturnIdAndOnCreateOrUpdate() {
        when(externalMandatoryFieldsExtractor.extract(AuditedType.INSTANCE)).thenReturn(Stream.empty());

        final AuditedEntityType<AuditedType> expectedAuditedEntityType =
            AuditedEntityType.builder(AuditedType.ID)
                             .withInternalFields(ON_CREATE_OR_UPDATE,
                                               AuditedType.NAME, AuditedType.DESC, AuditedType.DESC2, AuditedType.AMOUNT)
                             .build();

        assertThat(auditedEntityTypeResolver.resolve(AuditedType.INSTANCE),
                   isPresentAndIs(expectedAuditedEntityType));
    }

    @Test
    public void resolve_WhenAudited_AndHasId_AndOnUpdate_ShouldReturnIdAndOnUpdate() {
        when(externalMandatoryFieldsExtractor.extract(AuditedWithOnUpdateType.INSTANCE)).thenReturn(Stream.empty());

        final AuditedEntityType<AuditedWithOnUpdateType> expectedAuditedEntityType =
            AuditedEntityType.builder(AuditedWithOnUpdateType.ID)
                             .withInternalFields(ON_UPDATE,
                                               AuditedWithOnUpdateType.NAME,
                                               AuditedWithOnUpdateType.DESC,
                                               AuditedWithOnUpdateType.DESC2)
                             .build();

        assertThat(auditedEntityTypeResolver.resolve(AuditedWithOnUpdateType.INSTANCE),
                   isPresentAndIs(expectedAuditedEntityType));
    }

    @Test
    public void resolve_WhenAudited_AndHasId_AndInternalMandatoryFields_ShouldReturnIdAndInternalMandatoryFields() {
        when(externalMandatoryFieldsExtractor.extract(AuditedWithInternalMandatoryOnlyType.INSTANCE)).thenReturn(Stream.empty());

        final AuditedEntityType<AuditedWithInternalMandatoryOnlyType> expectedAuditedEntityType =
            AuditedEntityType.builder(AuditedWithInternalMandatoryOnlyType.ID)
                             .withInternalFields(ALWAYS, AuditedWithInternalMandatoryOnlyType.NAME)
                             .build();

        assertThat(auditedEntityTypeResolver.resolve(AuditedWithInternalMandatoryOnlyType.INSTANCE),
                   isPresentAndIs(expectedAuditedEntityType));
    }

    @Test
    public void resolve_WhenAudited_AndHasId_AndExternalMandatoryFields_AndOtherFields_ShouldReturnIdAndExternalMandatoryAndOnChange() {
        doReturn(Stream.of(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC))
            .when(externalMandatoryFieldsExtractor).extract(AuditedWithAncestorMandatoryType.INSTANCE);

        final AuditedEntityType<AuditedWithAncestorMandatoryType> expectedAuditedEntityType =
            AuditedEntityType.builder(AuditedWithAncestorMandatoryType.ID)
                             .withInternalFields(ON_CREATE_OR_UPDATE,
                                               AuditedWithAncestorMandatoryType.NAME,
                                               AuditedWithAncestorMandatoryType.DESC,
                                               AuditedWithAncestorMandatoryType.DESC2)
                             .withExternalFields(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC)
                             .build();

        assertThat(auditedEntityTypeResolver.resolve(AuditedWithAncestorMandatoryType.INSTANCE),
                   isPresentAndIs(expectedAuditedEntityType));
    }

    @Test
    public void resolve_WhenAudited_AndHasEverything_ShouldReturnEverything() {
        doReturn(Stream.of(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC))
            .when(externalMandatoryFieldsExtractor).extract(AuditedWithAllVariationsType.INSTANCE);

        final AuditedEntityType<AuditedWithAllVariationsType> expectedAuditedEntityType =
            AuditedEntityType.builder(AuditedWithAllVariationsType.ID)
                             .withExternalFields(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC)
                             .withInternalFields(ALWAYS, AuditedWithAllVariationsType.NAME)
                             .withInternalFields(ON_CREATE_OR_UPDATE, AuditedWithAllVariationsType.DESC)
                             .withInternalFields(ON_UPDATE, AuditedWithAllVariationsType.DESC2)
                             .build();

        assertThat(auditedEntityTypeResolver.resolve(AuditedWithAllVariationsType.INSTANCE),
                   isPresentAndIs(expectedAuditedEntityType));
    }

    @Test
    public void resolve_WhenInclusiveAudited_AndHasId_ShouldReturnIdAndOnCreateOrUpdateForIncludedFields() {
        when(externalMandatoryFieldsExtractor.extract(InclusiveAuditedType.INSTANCE)).thenReturn(Stream.empty());

        final AuditedEntityType<InclusiveAuditedType> expectedAuditedEntityType =
            AuditedEntityType.builder(InclusiveAuditedType.ID)
                             .withInternalFields(ON_CREATE_OR_UPDATE,
                                               InclusiveAuditedType.NAME, InclusiveAuditedType.DESC)
                             .build();

        assertThat(auditedEntityTypeResolver.resolve(InclusiveAuditedType.INSTANCE),
                   isPresentAndIs(expectedAuditedEntityType));
    }

    @Test
    public void resolve_WhenExclusiveAudited_AndHasId_ShouldReturnIdAndOnCreateOrUpdateForNotExcludedFields() {
        when(externalMandatoryFieldsExtractor.extract(ExclusiveAuditedType.INSTANCE)).thenReturn(Stream.empty());

        final AuditedEntityType<ExclusiveAuditedType> expectedAuditedEntityType =
            AuditedEntityType.builder(ExclusiveAuditedType.ID)
                             .withInternalFields(ON_CREATE_OR_UPDATE, ImmutableSet.of(ExclusiveAuditedType.NAME))
                             .build();

        assertThat(auditedEntityTypeResolver.resolve(ExclusiveAuditedType.INSTANCE),
                   isPresentAndIs(expectedAuditedEntityType));
    }

    @Test
    public void resolve_WhenAudited_AndHasNoId_ShouldReturnEmpty() {
        assertThat(auditedEntityTypeResolver.resolve(TestAuditedEntityWithoutIdType.INSTANCE), isEmpty());
    }

    @Test
    public void resolve_WhenNotAudited_AndHasId_ShouldReturnEmpty() {
        assertThat(auditedEntityTypeResolver.resolve(NotAuditedType.INSTANCE), isEmpty());
    }

    @Test
    public void resolve_WhenNotAudited_AndHasNoId_ShouldReturnEmpty() {
        assertThat(auditedEntityTypeResolver.resolve(TestEntityWithoutIdType.INSTANCE), isEmpty());
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