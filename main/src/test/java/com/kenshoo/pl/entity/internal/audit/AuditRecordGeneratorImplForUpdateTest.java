package com.kenshoo.pl.entity.internal.audit;

import com.google.common.collect.ImmutableList;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.audit.AuditProperties;
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

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAnd;
import static com.kenshoo.pl.entity.ChangeOperation.UPDATE;
import static com.kenshoo.pl.entity.matchers.audit.AuditRecordMatchers.*;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.allOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuditRecordGeneratorImplForUpdateTest {

    private static final String ENTITY_TYPE_NAME = "someEntityType";
    private static final long ID = 1234;
    private static final String STRING_ID = String.valueOf(ID);
    private static final String ANCESTOR_NAME = "ancestorName";
    private static final String ANCESTOR_DESC = "ancestorDesc";
    private static final String NEW_NAME = "newName";
    private static final String OLD_NAME = "oldName";
    private static final String NEW_DESC = "newDesc";
    private static final String OLD_DESC = "oldDesc";
    private static final String ENTITY_CHANGE_DESCRIPTION = "A very interesting description";

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
        when(cmd.getChangeOperation()).thenReturn(UPDATE);
        when(changeContext.getEntity(cmd)).thenReturn(currentState);
        when(changeContext.getFinalEntity(cmd)).thenReturn(finalState);
        when(entityIdExtractor.extract(cmd, currentState)).thenReturn(Optional.empty());
        when(mandatoryFieldValuesGenerator.generate(finalState)).thenReturn(emptyList());
        when(fieldChangesGenerator.generate(currentState, finalState)).thenReturn(emptyList());

        auditRecordGenerator = new AuditRecordGeneratorImpl<>(mandatoryFieldValuesGenerator,
                                                              fieldChangesGenerator,
                                                              entityIdExtractor,
                                                              ENTITY_TYPE_NAME);
    }

    @Test
    public void generate_WithIdOnly_ShouldReturnEmpty() {
        when(entityIdExtractor.extract(cmd, currentState)).thenReturn(Optional.of(STRING_ID));

        final Optional<? extends AuditRecord> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, changeContext, emptyList());

        assertThat(actualOptionalAuditRecord, isEmpty());
    }

    @Test
    public void generate_WithIdAndMandatoryOnly_ShouldReturnEmpty() {
        final Collection<FieldValue> expectedMandatoryFieldValues =
            List.of(new FieldValue(NotAuditedAncestorType.NAME.toString(), ANCESTOR_NAME),
                    new FieldValue(NotAuditedAncestorType.DESC.toString(), ANCESTOR_DESC));

        when(entityIdExtractor.extract(cmd, currentState)).thenReturn(Optional.of(STRING_ID));
        when(mandatoryFieldValuesGenerator.generate(finalState)).thenReturn(expectedMandatoryFieldValues);

        final Optional<? extends AuditRecord> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, changeContext, emptyList());

        assertThat(actualOptionalAuditRecord, isEmpty());
    }

    @Test
    public void generate_WithIdAndFieldChangesOnly_ShouldReturnIdTypeAndOperatorAndFieldChanges() {
        final Collection<FieldAuditRecord> expectedFieldChanges =
            ImmutableList.of(FieldAuditRecord.builder(AuditedAutoIncIdType.NAME)
                                             .oldValue(OLD_NAME)
                                             .newValue(NEW_NAME)
                                             .build(),
                             FieldAuditRecord.builder(AuditedAutoIncIdType.DESC)
                                             .oldValue(OLD_DESC)
                                             .newValue(NEW_DESC)
                                             .build());

        when(entityIdExtractor.extract(cmd, currentState)).thenReturn(Optional.of(STRING_ID));
        when(fieldChangesGenerator.generate(currentState, finalState)).thenReturn(expectedFieldChanges);

        final Optional<? extends AuditRecord> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, changeContext, emptyList());

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasEntityId(STRING_ID),
                                      hasEntityType(ENTITY_TYPE_NAME),
                                      hasOperator(UPDATE),
                                      hasChangedFieldRecord(AuditedAutoIncIdType.NAME, OLD_NAME, NEW_NAME),
                                      hasChangedFieldRecord(AuditedAutoIncIdType.DESC, OLD_DESC, NEW_DESC))));
    }

    @Test
    public void generate_WithIdAndChildRecordsOnly_ShouldReturnIdTypeAndOperatorAndChildRecords() {
        final Collection<FieldValue> expectedMandatoryFieldValues =
            ImmutableList.of(new FieldValue(NotAuditedAncestorType.NAME.toString(), ANCESTOR_NAME),
                             new FieldValue(NotAuditedAncestorType.DESC.toString(), ANCESTOR_DESC));

        when(entityIdExtractor.extract(cmd, currentState)).thenReturn(Optional.of(STRING_ID));
        when(mandatoryFieldValuesGenerator.generate(finalState)).thenReturn(expectedMandatoryFieldValues);
        when(fieldChangesGenerator.generate(currentState, finalState)).thenReturn(emptyList());

        final List<AuditRecord> childRecords = ImmutableList.of(mockChildRecord(), mockChildRecord());

        final Optional<? extends AuditRecord> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, changeContext, childRecords);

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasEntityId(STRING_ID),
                                      hasEntityType(ENTITY_TYPE_NAME),
                                      hasOperator(UPDATE),
                                      hasSameChildRecord(childRecords.get(0)),
                                      hasSameChildRecord(childRecords.get(1)))));
    }

    @Test
    public void generate_WithEverything_ShouldGenerateEverything() {
        final Collection<FieldValue> expectedMandatoryFieldValues =
            List.of(new FieldValue(NotAuditedAncestorType.NAME.toString(), ANCESTOR_NAME),
                    new FieldValue(NotAuditedAncestorType.DESC.toString(), ANCESTOR_DESC));

        final Collection<FieldAuditRecord> expectedFieldChanges =
            ImmutableList.of(FieldAuditRecord.builder(AuditedAutoIncIdType.NAME)
                                             .oldValue(OLD_NAME)
                                             .newValue(NEW_NAME)
                                             .build(),
                             FieldAuditRecord.builder(AuditedAutoIncIdType.DESC)
                                             .oldValue(OLD_DESC)
                                             .newValue(NEW_DESC)
                                             .build());

        when(entityIdExtractor.extract(cmd, currentState)).thenReturn(Optional.of(STRING_ID));
        when(mandatoryFieldValuesGenerator.generate(finalState)).thenReturn(expectedMandatoryFieldValues);
        when(fieldChangesGenerator.generate(currentState, finalState)).thenReturn(expectedFieldChanges);

        final List<AuditRecord> childRecords = ImmutableList.of(mockChildRecord(), mockChildRecord());

        final Optional<? extends AuditRecord> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, changeContext, childRecords);

        //noinspection unchecked
        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasEntityId(STRING_ID),
                                      hasEntityType(ENTITY_TYPE_NAME),
                                      hasOperator(UPDATE),
                                      hasMandatoryFieldValue(NotAuditedAncestorType.NAME, ANCESTOR_NAME),
                                      hasMandatoryFieldValue(NotAuditedAncestorType.DESC, ANCESTOR_DESC),
                                      hasChangedFieldRecord(AuditedAutoIncIdType.NAME, OLD_NAME, NEW_NAME),
                                      hasChangedFieldRecord(AuditedAutoIncIdType.DESC, OLD_DESC, NEW_DESC),
                                      hasSameChildRecord(childRecords.get(0)),
                                      hasSameChildRecord(childRecords.get(1)))));
    }

    @Test
    public void generate_WithIdAndFieldChanges_AndEntityChangeDescriptionInCmd_ShouldGenerateWithEntityChangeDescription() {
        final var expectedFieldChanges = List.of(
                FieldAuditRecord.builder(AuditedAutoIncIdType.NAME)
                        .oldValue(OLD_NAME)
                        .newValue(NEW_NAME)
                        .build());

        when(entityIdExtractor.extract(cmd, currentState)).thenReturn(Optional.of(STRING_ID));
        when(fieldChangesGenerator.generate(currentState, finalState)).thenReturn(expectedFieldChanges);
        when(cmd.get(AuditProperties.ENTITY_CHANGE_DESCRIPTION)).thenReturn(Optional.of(ENTITY_CHANGE_DESCRIPTION));

        final Optional<? extends AuditRecord> actualOptionalAuditRecord =
                auditRecordGenerator.generate(cmd, changeContext, emptyList());

        assertThat(actualOptionalAuditRecord,
                isPresentAnd(hasEntityChangeDescription(ENTITY_CHANGE_DESCRIPTION)));
    }

    @Test
    public void generate_WhenEmpty_ShouldReturnEmpty() {
        final Optional<? extends AuditRecord> actualOptionalAuditRecord =
                auditRecordGenerator.generate(cmd, changeContext, emptyList());

        assertThat(actualOptionalAuditRecord, isEmpty());
    }

    @Test
    public void generate_WithoutIdAndWithMandatoryOnly_ShouldReturnEmpty() {
        final Collection<FieldValue> expectedMandatoryFieldValues =
                List.of(new FieldValue(NotAuditedAncestorType.NAME.toString(), ANCESTOR_NAME),
                        new FieldValue(NotAuditedAncestorType.DESC.toString(), ANCESTOR_DESC));

        when(mandatoryFieldValuesGenerator.generate(finalState)).thenReturn(expectedMandatoryFieldValues);

        final Optional<? extends AuditRecord> actualOptionalAuditRecord =
                auditRecordGenerator.generate(cmd, changeContext, emptyList());

        assertThat(actualOptionalAuditRecord, isEmpty());
    }

    @Test
    public void generate_WithoutIdAndWithFieldChangesOnly_ShouldReturnTypeAndOperatorAndFieldChanges() {
        final Collection<FieldAuditRecord> expectedFieldChanges =
                ImmutableList.of(FieldAuditRecord.builder(AuditedAutoIncIdType.NAME)
                                .oldValue(OLD_NAME)
                                .newValue(NEW_NAME)
                                .build(),
                        FieldAuditRecord.builder(AuditedAutoIncIdType.DESC)
                                .oldValue(OLD_DESC)
                                .newValue(NEW_DESC)
                                .build());

        when(fieldChangesGenerator.generate(currentState, finalState)).thenReturn(expectedFieldChanges);

        final Optional<? extends AuditRecord> actualOptionalAuditRecord =
                auditRecordGenerator.generate(cmd, changeContext, emptyList());

        assertThat(actualOptionalAuditRecord,
                isPresentAnd(allOf(hasEntityType(ENTITY_TYPE_NAME),
                        hasOperator(UPDATE),
                        hasChangedFieldRecord(AuditedAutoIncIdType.NAME, OLD_NAME, NEW_NAME),
                        hasChangedFieldRecord(AuditedAutoIncIdType.DESC, OLD_DESC, NEW_DESC))));
    }

    @Test
    public void generate_WithoutIdAndWithChildRecordsOnly_ShouldReturnTypeAndOperatorAndChildRecords() {
        final Collection<FieldValue> expectedMandatoryFieldValues =
                ImmutableList.of(new FieldValue(NotAuditedAncestorType.NAME.toString(), ANCESTOR_NAME),
                        new FieldValue(NotAuditedAncestorType.DESC.toString(), ANCESTOR_DESC));

        when(mandatoryFieldValuesGenerator.generate(finalState)).thenReturn(expectedMandatoryFieldValues);

        final List<AuditRecord> childRecords = ImmutableList.of(mockChildRecord(), mockChildRecord());

        final Optional<? extends AuditRecord> actualOptionalAuditRecord =
                auditRecordGenerator.generate(cmd, changeContext, childRecords);

        assertThat(actualOptionalAuditRecord,
                isPresentAnd(allOf(hasEntityType(ENTITY_TYPE_NAME),
                        hasOperator(UPDATE),
                        hasSameChildRecord(childRecords.get(0)),
                        hasSameChildRecord(childRecords.get(1)))));
    }

    private AuditRecord mockChildRecord() {
        return mock(AuditRecord.class);
    }
}