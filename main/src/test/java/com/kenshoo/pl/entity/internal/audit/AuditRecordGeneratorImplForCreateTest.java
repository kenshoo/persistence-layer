package com.kenshoo.pl.entity.internal.audit;

import com.google.common.collect.ImmutableList;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.audit.AuditRecord;
import com.kenshoo.pl.entity.audit.FieldAuditRecord;
import com.kenshoo.pl.entity.internal.EntityIdExtractor;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedAutoIncIdType;
import com.kenshoo.pl.entity.internal.audit.entitytypes.NotAuditedAncestorType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAnd;
import static com.kenshoo.pl.entity.ChangeOperation.CREATE;
import static com.kenshoo.pl.entity.matchers.audit.AuditRecordMatchers.*;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.allOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuditRecordGeneratorImplForCreateTest {

    private static final String ENTITY_TYPE_NAME = "someEntityType";
    private static final long ID = 1234;
    private static final String STRING_ID = String.valueOf(ID);
    private static final String NAME = "name";
    private static final String DESC = "desc";
    private static final String ANCESTOR_NAME = "ancestorName";
    private static final String ANCESTOR_DESC = "ancestorDesc";

    @Mock
    private AuditMandatoryFieldValuesGenerator mandatoryFieldValuesGenerator;

    @Mock
    private AuditFieldChangesGenerator<AuditedAutoIncIdType> fieldChangesGenerator;

    @Mock
    private EntityIdExtractor entityIdExtractor;

    @Mock
    private ChangeContext changeContext;

    @Mock
    private CurrentEntityState currentState;

    @Mock
    private EntityChange<AuditedAutoIncIdType> cmd;

    @Mock
    private FinalEntityState finalState;

    private AuditRecordGeneratorImpl<AuditedAutoIncIdType> auditRecordGenerator;

    @Before
    public void setUp() {
        when(entityIdExtractor.extract(cmd, currentState)).thenReturn(Optional.empty());
        when(cmd.getChangeOperation()).thenReturn(CREATE);
        when(changeContext.getEntity(cmd)).thenReturn(currentState);
        when(changeContext.getFinalEntity(cmd)).thenReturn(finalState);

        auditRecordGenerator = new AuditRecordGeneratorImpl<>(mandatoryFieldValuesGenerator,
                                                              fieldChangesGenerator,
                                                              entityIdExtractor,
                                                              ENTITY_TYPE_NAME);
    }

    @Test
    public void generate_WithIdOnly_ShouldReturnIdTypeAndOperator() {
        when(entityIdExtractor.extract(cmd, currentState)).thenReturn(Optional.of(STRING_ID));
        when(mandatoryFieldValuesGenerator.generate(finalState)).thenReturn(emptyList());
        when(fieldChangesGenerator.generate(currentState, finalState)).thenReturn(emptyList());

        final Optional<? extends AuditRecord> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, changeContext, emptyList());

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasEntityType(ENTITY_TYPE_NAME),
                                      hasEntityId(STRING_ID),
                                      hasOperator(CREATE))));
    }

    @Test
    public void generate_WithIdAndMandatoryOnly_ShouldReturnMandatoryFieldValues() {
        final Collection<FieldValue> expectedMandatoryFieldValues =
            List.of(new FieldValue(NotAuditedAncestorType.NAME.toString(), ANCESTOR_NAME),
                    new FieldValue(NotAuditedAncestorType.DESC.toString(), ANCESTOR_DESC));

        when(entityIdExtractor.extract(cmd, currentState)).thenReturn(Optional.of(STRING_ID));
        when(mandatoryFieldValuesGenerator.generate(finalState)).thenReturn(expectedMandatoryFieldValues);
        when(fieldChangesGenerator.generate(currentState, finalState)).thenReturn(emptyList());

        final Optional<? extends AuditRecord> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, changeContext, emptyList());

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasMandatoryFieldValue(NotAuditedAncestorType.NAME, ANCESTOR_NAME),
                                      hasMandatoryFieldValue(NotAuditedAncestorType.DESC, ANCESTOR_DESC))));
    }

    @Test
    public void generate_WithIdAndFieldChangesOnly_ShouldReturnFieldChanges() {
        final Collection<FieldAuditRecord> expectedFieldChanges =
            ImmutableList.of(FieldAuditRecord.builder(AuditedAutoIncIdType.NAME)
                                             .newValue(NAME)
                                             .build(),
                             FieldAuditRecord.builder(AuditedAutoIncIdType.DESC)
                                             .newValue(DESC)
                                             .build());

        when(entityIdExtractor.extract(cmd, currentState)).thenReturn(Optional.of(STRING_ID));
        when(mandatoryFieldValuesGenerator.generate(finalState)).thenReturn(emptyList());
        when(fieldChangesGenerator.generate(currentState, finalState)).thenReturn(expectedFieldChanges);

        final Optional<? extends AuditRecord> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, changeContext, emptyList());

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasCreatedFieldRecord(AuditedAutoIncIdType.NAME, NAME),
                                      hasCreatedFieldRecord(AuditedAutoIncIdType.DESC, DESC))));
    }

    @Test
    public void generate_WithIdAndChildRecordsOnly_ShouldReturnChildRecords() {
        when(entityIdExtractor.extract(cmd, currentState)).thenReturn(Optional.of(STRING_ID));
        when(mandatoryFieldValuesGenerator.generate(finalState)).thenReturn(emptyList());
        when(fieldChangesGenerator.generate(currentState, finalState)).thenReturn(emptyList());

        final List<AuditRecord> childRecords = ImmutableList.of(mockChildRecord(), mockChildRecord());

        final Optional<? extends AuditRecord> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, changeContext, childRecords);

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasSameChildRecord(childRecords.get(0)),
                                      hasSameChildRecord(childRecords.get(1)))));
    }

    @Test
    public void generate_WithIdAndMandatoryAndFieldChangesOnly_ShouldReturnMandatoryFieldValuesAndFieldChanges() {
        final Collection<FieldValue> expectedMandatoryFieldValues =
            List.of(new FieldValue(NotAuditedAncestorType.NAME.toString(), ANCESTOR_NAME),
                    new FieldValue(NotAuditedAncestorType.DESC.toString(), ANCESTOR_DESC));

        final Collection<FieldAuditRecord> expectedFieldChanges =
            ImmutableList.of(FieldAuditRecord.builder(AuditedAutoIncIdType.NAME)
                                             .newValue(NAME)
                                             .build(),
                             FieldAuditRecord.builder(AuditedAutoIncIdType.DESC)
                                             .newValue(DESC)
                                             .build());

        when(entityIdExtractor.extract(cmd, currentState)).thenReturn(Optional.of(STRING_ID));
        when(mandatoryFieldValuesGenerator.generate(finalState)).thenReturn(expectedMandatoryFieldValues);
        when(fieldChangesGenerator.generate(currentState, finalState)).thenReturn(expectedFieldChanges);

        final Optional<? extends AuditRecord> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, changeContext, emptyList());

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasMandatoryFieldValue(NotAuditedAncestorType.NAME, ANCESTOR_NAME),
                                      hasMandatoryFieldValue(NotAuditedAncestorType.DESC, ANCESTOR_DESC),
                                      hasCreatedFieldRecord(AuditedAutoIncIdType.NAME, NAME),
                                      hasCreatedFieldRecord(AuditedAutoIncIdType.DESC, DESC))));
    }

    @Test
    public void generate_WithEverything_ShouldReturnEverything() {
        final Collection<FieldValue> expectedMandatoryFieldValues =
            List.of(new FieldValue(NotAuditedAncestorType.NAME.toString(), ANCESTOR_NAME),
                    new FieldValue(NotAuditedAncestorType.DESC.toString(), ANCESTOR_DESC));

        final Collection<FieldAuditRecord> expectedFieldChanges =
            ImmutableList.of(FieldAuditRecord.builder(AuditedAutoIncIdType.NAME)
                                             .newValue(NAME)
                                             .build(),
                             FieldAuditRecord.builder(AuditedAutoIncIdType.DESC)
                                             .newValue(DESC)
                                             .build());

        when(entityIdExtractor.extract(cmd, currentState)).thenReturn(Optional.of(STRING_ID));
        when(mandatoryFieldValuesGenerator.generate(finalState)).thenReturn(expectedMandatoryFieldValues);
        when(fieldChangesGenerator.generate(currentState, finalState)).thenReturn(expectedFieldChanges);

        final List<AuditRecord> childRecords = ImmutableList.of(mockChildRecord(), mockChildRecord());

        final Optional<? extends AuditRecord> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, changeContext, childRecords);

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasMandatoryFieldValue(NotAuditedAncestorType.NAME, ANCESTOR_NAME),
                                      hasMandatoryFieldValue(NotAuditedAncestorType.DESC, ANCESTOR_DESC),
                                      hasCreatedFieldRecord(AuditedAutoIncIdType.NAME, NAME),
                                      hasCreatedFieldRecord(AuditedAutoIncIdType.DESC, DESC),
                                      hasSameChildRecord(childRecords.get(0)),
                                      hasSameChildRecord(childRecords.get(1)))));
    }

    @Test
    public void generate_WhenEmpty_ShouldReturnTypeAndOperator() {
        final var actualOptionalAuditRecord = auditRecordGenerator.generate(cmd, changeContext, emptyList());

        assertThat(actualOptionalAuditRecord,
                isPresentAnd(allOf(hasEntityType(ENTITY_TYPE_NAME),
                        hasOperator(CREATE))));
    }

    @Test
    public void generate_WithoutIdAndWithMandatoryOnly_ShouldReturnMandatoryFieldValues() {
        final var expectedMandatoryFieldValues = List.of(
                new FieldValue(NotAuditedAncestorType.NAME.toString(), ANCESTOR_NAME),
                new FieldValue(NotAuditedAncestorType.DESC.toString(), ANCESTOR_DESC));

        when(mandatoryFieldValuesGenerator.generate(finalState)).thenReturn(expectedMandatoryFieldValues);

        final var actualOptionalAuditRecord = auditRecordGenerator.generate(cmd, changeContext, emptyList());

        assertThat(actualOptionalAuditRecord,
                isPresentAnd(allOf(hasMandatoryFieldValue(NotAuditedAncestorType.NAME, ANCESTOR_NAME),
                        hasMandatoryFieldValue(NotAuditedAncestorType.DESC, ANCESTOR_DESC))));
    }

    @Test
    public void generate_WithoutIdAndWithFieldChangesOnly_ShouldReturnFieldChanges() {
        final var expectedFieldChanges = List.of(
                FieldAuditRecord.builder(AuditedAutoIncIdType.NAME)
                        .newValue(NAME)
                        .build(),
                FieldAuditRecord.builder(AuditedAutoIncIdType.DESC)
                        .newValue(DESC)
                        .build());

        when(fieldChangesGenerator.generate(currentState, finalState)).thenReturn(expectedFieldChanges);

        final var actualOptionalAuditRecord = auditRecordGenerator.generate(cmd, changeContext, emptyList());

        assertThat(actualOptionalAuditRecord,
                isPresentAnd(allOf(hasCreatedFieldRecord(AuditedAutoIncIdType.NAME, NAME),
                        hasCreatedFieldRecord(AuditedAutoIncIdType.DESC, DESC))));
    }

    @Test
    public void generate_WithoutIdAndWithChildRecordsOnly_ShouldReturnChildRecords() {
        final var childRecords = List.of(mockChildRecord(), mockChildRecord());

        final Optional<? extends AuditRecord> actualOptionalAuditRecord =
                auditRecordGenerator.generate(cmd, changeContext, childRecords);

        assertThat(actualOptionalAuditRecord,
                isPresentAnd(allOf(hasSameChildRecord(childRecords.get(0)),
                        hasSameChildRecord(childRecords.get(1)))));
    }


    private AuditRecord mockChildRecord() {
        return mock(AuditRecord.class);
    }
}