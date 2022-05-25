package com.kenshoo.pl.entity.internal.audit;

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

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAndIs;
import static com.kenshoo.pl.entity.audit.AuditTrigger.*;
import static com.kenshoo.pl.entity.internal.audit.AuditIndicator.AUDITED;
import static com.kenshoo.pl.entity.internal.audit.AuditIndicator.NOT_AUDITED;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuditedEntityTypeResolverImplTest {

    private static final String ENTITY_TYPE_NAME = "SomeEntity";

    @Mock
    private AuditedEntityTypeNameResolver auditedEntityTypeNameResolver;

    @Mock
    private AuditedFieldResolver auditedFieldResolver;

    @Mock
    private ExternalMandatoryFieldsExtractor externalMandatoryFieldsExtractor;

    @InjectMocks
    private AuditedEntityTypeResolverImpl auditedEntityTypeResolver;

    @Test
    public void resolve_WhenAudited_AndHasId_AndOneInternalType_ShouldReturnAllFields() {
        when(auditedEntityTypeNameResolver.resolve(AuditedAutoIncIdType.INSTANCE)).thenReturn(ENTITY_TYPE_NAME);
        when(externalMandatoryFieldsExtractor.extract(AuditedAutoIncIdType.INSTANCE)).thenReturn(Stream.empty());

        final var expectedAuditedFieldMap = Map.of(AuditedAutoIncIdType.NAME,
                                                   AuditedField.builder(AuditedAutoIncIdType.NAME)
                                                               .withName("name")
                                                               .withTrigger(ON_CREATE_OR_UPDATE)
                                                               .build(),
                                                   AuditedAutoIncIdType.DESC,
                                                   AuditedField.builder(AuditedAutoIncIdType.DESC)
                                                               .withName("desc")
                                                               .withTrigger(ON_CREATE_OR_UPDATE)
                                                               .build());

        expectedAuditedFieldMap.forEach((field, expectedAuditedField) ->
                                            when(auditedFieldResolver.resolve(field, AUDITED)).thenReturn(Optional.of(expectedAuditedField)));

        final var expectedAuditedEntityType =
            AuditedEntityType.builder(AuditedAutoIncIdType.ID)
                             .withName(ENTITY_TYPE_NAME)
                             .withInternalFields(ON_CREATE_OR_UPDATE, expectedAuditedFieldMap.values())
                             .build();

        assertThat(auditedEntityTypeResolver.resolve(AuditedAutoIncIdType.INSTANCE),
                   isPresentAndIs(expectedAuditedEntityType));
    }

    @Test
    public void resolve_WhenAudited_AndHasId_AndTwoInternalTypes_ShouldReturnAllFields() {
        when(auditedEntityTypeNameResolver.resolve(AuditedAutoIncIdType.INSTANCE)).thenReturn(ENTITY_TYPE_NAME);
        when(externalMandatoryFieldsExtractor.extract(AuditedAutoIncIdType.INSTANCE)).thenReturn(Stream.empty());

        final var expectedAuditedFieldMap = Map.of(AuditedAutoIncIdType.NAME,
                                                   AuditedField.builder(AuditedAutoIncIdType.NAME)
                                                               .withName("name")
                                                               .withTrigger(ON_CREATE_OR_UPDATE)
                                                               .build(),
                                                   AuditedAutoIncIdType.DESC,
                                                   AuditedField.builder(AuditedAutoIncIdType.DESC)
                                                               .withName("desc")
                                                               .withTrigger(ON_CREATE_OR_UPDATE)
                                                               .build(),
                                                   AuditedAutoIncIdType.DESC2,
                                                   AuditedField.builder(AuditedAutoIncIdType.DESC2)
                                                               .withName("desc2")
                                                               .withTrigger(ON_UPDATE)
                                                               .build());

        expectedAuditedFieldMap.forEach((field, expectedAuditedField) ->
                                            when(auditedFieldResolver.resolve(field, AUDITED)).thenReturn(Optional.of(expectedAuditedField)));

        final var expectedAuditedEntityType =
            AuditedEntityType.builder(AuditedAutoIncIdType.ID)
                             .withName(ENTITY_TYPE_NAME)
                             .withInternalFields(ON_CREATE_OR_UPDATE, Set.of(expectedAuditedFieldMap.get(AuditedAutoIncIdType.NAME),
                                                                             expectedAuditedFieldMap.get(AuditedAutoIncIdType.DESC)))
                             .withInternalFields(ON_UPDATE, Set.of(expectedAuditedFieldMap.get(AuditedAutoIncIdType.DESC2)))
                             .build();

        assertThat(auditedEntityTypeResolver.resolve(AuditedAutoIncIdType.INSTANCE),
                   isPresentAndIs(expectedAuditedEntityType));
    }

    @Test
    public void resolve_WhenAudited_AndHasId_AndInternalFields_AndExternalFields_ShouldReturnAllFields() {
        final var expectedAuditedInternalFieldMap = Map.of(AuditedAutoIncIdType.NAME,
                                                           AuditedField.builder(AuditedAutoIncIdType.NAME)
                                                                       .withName("name")
                                                                       .withTrigger(ON_CREATE_OR_UPDATE)
                                                                       .build(),
                                                           AuditedAutoIncIdType.DESC,
                                                           AuditedField.builder(AuditedAutoIncIdType.DESC)
                                                                       .withName("desc")
                                                                       .withTrigger(ON_CREATE_OR_UPDATE)
                                                                       .build());

        final var expectedAuditedExternalFields = Set.of(AuditedField.builder(AuditedWithAncestorMandatoryType.NAME)
                                                                     .withName("ancestor_name")
                                                                     .withTrigger(ALWAYS)
                                                                     .build(),
                                                         AuditedField.builder(AuditedWithAncestorMandatoryType.DESC)
                                                                     .withName("ancestor_desc")
                                                                     .withTrigger(ALWAYS)
                                                                     .build());

        when(auditedEntityTypeNameResolver.resolve(AuditedAutoIncIdType.INSTANCE)).thenReturn(ENTITY_TYPE_NAME);
        doReturn(expectedAuditedExternalFields.stream()).when(externalMandatoryFieldsExtractor).extract(AuditedAutoIncIdType.INSTANCE);

        expectedAuditedInternalFieldMap.forEach((field, expectedAuditedField) ->
                                                    when(auditedFieldResolver.resolve(field, AUDITED)).thenReturn(Optional.of(expectedAuditedField)));

        final var expectedAuditedEntityType =
            AuditedEntityType.builder(AuditedAutoIncIdType.ID)
                             .withName(ENTITY_TYPE_NAME)
                             .withExternalFields(expectedAuditedExternalFields)
                             .withInternalFields(ON_CREATE_OR_UPDATE, Set.of(expectedAuditedInternalFieldMap.get(AuditedAutoIncIdType.NAME),
                                                                             expectedAuditedInternalFieldMap.get(AuditedAutoIncIdType.DESC)))
                             .build();

        assertThat(auditedEntityTypeResolver.resolve(AuditedAutoIncIdType.INSTANCE),
                   isPresentAndIs(expectedAuditedEntityType));
    }

    @Test
    public void resolve_WhenNotAudited_AndHasId_AndInternalFields_ShouldReturnPopulatedObject() {
        when(auditedEntityTypeNameResolver.resolve(InclusiveAuditedType.INSTANCE)).thenReturn(ENTITY_TYPE_NAME);
        when(externalMandatoryFieldsExtractor.extract(InclusiveAuditedType.INSTANCE)).thenReturn(Stream.empty());

        final var expectedAuditedFieldMap = Map.of(InclusiveAuditedType.NAME,
                                                   AuditedField.builder(InclusiveAuditedType.NAME)
                                                               .withName("name")
                                                               .withTrigger(ON_CREATE_OR_UPDATE)
                                                               .build(),
                                                   InclusiveAuditedType.DESC,
                                                   AuditedField.builder(InclusiveAuditedType.DESC)
                                                               .withName("desc")
                                                               .withTrigger(ON_CREATE_OR_UPDATE)
                                                               .build());

        expectedAuditedFieldMap.forEach((field, expectedAuditedField) ->
                                            when(auditedFieldResolver.resolve(field, NOT_AUDITED)).thenReturn(Optional.of(expectedAuditedField)));

        final var expectedAuditedEntityType =
            AuditedEntityType.builder(InclusiveAuditedType.ID)
                             .withName(ENTITY_TYPE_NAME)
                             .withInternalFields(ON_CREATE_OR_UPDATE, expectedAuditedFieldMap.values())
                             .build();

        assertThat(auditedEntityTypeResolver.resolve(InclusiveAuditedType.INSTANCE),
                   isPresentAndIs(expectedAuditedEntityType));
    }

    @Test
    public void resolve_WhenAudited_AndHasNoId_ShouldReturnEmpty() {
        assertThat(auditedEntityTypeResolver.resolve(TestAuditedEntityWithoutIdType.INSTANCE), isEmpty());
    }

    @Test
    public void resolve_WhenNotAudited_AndHasId_ShouldReturnEmpty() {
        when(auditedEntityTypeNameResolver.resolve(NotAuditedType.INSTANCE)).thenReturn(ENTITY_TYPE_NAME);
        assertThat(auditedEntityTypeResolver.resolve(NotAuditedType.INSTANCE), isEmpty());
    }

    @Test
    public void resolve_WhenNotAudited_AndHasNoId_ShouldReturnEmpty() {
        assertThat(auditedEntityTypeResolver.resolve(TestEntityWithoutIdType.INSTANCE), isEmpty());
    }

    @Audited
    public static class TestAuditedEntityWithoutIdType extends AbstractAutoIncIdType<TestAuditedEntityWithoutIdType> {

        public static final TestAuditedEntityWithoutIdType INSTANCE = new TestAuditedEntityWithoutIdType();

        public static final EntityField<TestAuditedEntityWithoutIdType, String> NAME = INSTANCE.field(MainAutoIncIdTable.INSTANCE.name);

        private TestAuditedEntityWithoutIdType() {
            super("TestAuditedEntityWithoutId");
        }
    }

    public static class TestEntityWithoutIdType extends AbstractEntityType<TestEntityWithoutIdType> {

        public static final TestEntityWithoutIdType INSTANCE = new TestEntityWithoutIdType();

        public static final EntityField<TestEntityWithoutIdType, String> NAME = INSTANCE.field(MainAutoIncIdTable.INSTANCE.name);

        @Override
        public DataTable getPrimaryTable() {
            return MainAutoIncIdTable.INSTANCE;
        }

        private TestEntityWithoutIdType() {
            super("TestEntityWithoutId");
        }
    }
}