package com.kenshoo.pl.entity.internal.audit;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.kenshoo.pl.entity.CurrentEntityMutableState;
import com.kenshoo.pl.entity.CurrentEntityState;
import com.kenshoo.pl.entity.audit.AuditRecord;
import com.kenshoo.pl.entity.internal.EntityIdExtractor;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType;
import com.kenshoo.pl.entity.internal.audit.entitytypes.NotAuditedAncestorType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Optional;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAnd;
import static com.kenshoo.pl.entity.ChangeOperation.CREATE;
import static com.kenshoo.pl.entity.audit.AuditTrigger.*;
import static com.kenshoo.pl.entity.matchers.audit.AuditRecordMatchers.*;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.allOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class AuditRecordGeneratorForCreateTest {

    private static final long ID = 1234;
    private static final String STRING_ID = String.valueOf(ID);
    private static final String NAME = "name";
    private static final String DESC = "desc";
    private static final String DESC2 = "desc2";
    private static final String ANCESTOR_NAME = "ancestorName";
    private static final String ANCESTOR_DESC = "ancestorDesc";

    @Mock
    private EntityIdExtractor entityIdExtractor;

    @Mock
    private AuditedFieldsToFetchResolver fieldsToFetchResolver;

    @Test
    public void generate_WithIdOnly_ShouldGenerateBasicData() {
        final AuditedCommand cmd = new AuditedCommand(ID, CREATE);

        final CurrentEntityState currentState = CurrentEntityState.EMPTY;

        final AuditedFieldSet<AuditedType> auditedFieldSet = AuditedFieldSet.builder(AuditedType.ID).build();
        final AuditRecordGeneratorStateConsumerImpl<AuditedType> auditRecordGenerator = newAuditRecordGenerator(auditedFieldSet);

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, emptyList());

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasEntityType(AuditedType.INSTANCE),
                                      hasEntityId(STRING_ID),
                                      hasOperator(CREATE))));
    }

    @Test
    public void generate_WithExternalMandatoryOnly_ShouldGenerateMandatoryFieldValues() {
        final AuditedCommand cmd = new AuditedCommand(ID, CREATE);

        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();
        currentState.set(NotAuditedAncestorType.NAME, ANCESTOR_NAME);
        currentState.set(NotAuditedAncestorType.DESC, ANCESTOR_DESC);

        final AuditedFieldSet<AuditedType> auditedFieldSet =
            AuditedFieldSet.builder(AuditedType.ID)
                           .withExternalFields(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC)
                           .build();
        final AuditRecordGeneratorStateConsumerImpl<AuditedType> auditRecordGenerator = newAuditRecordGenerator(auditedFieldSet);

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, emptyList());

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasMandatoryFieldValue(NotAuditedAncestorType.NAME, ANCESTOR_NAME),
                                      hasMandatoryFieldValue(NotAuditedAncestorType.DESC, ANCESTOR_DESC))));
    }

    @Test
    public void generate_WithExternalMandatoryHavingNullValues_ShouldNotGenerateMandatoryFieldValues() {
        final AuditedCommand cmd = new AuditedCommand(ID, CREATE);

        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();
        currentState.set(NotAuditedAncestorType.NAME, null);
        currentState.set(NotAuditedAncestorType.DESC, null);

        final AuditedFieldSet<AuditedType> auditedFieldSet =
            AuditedFieldSet.builder(AuditedType.ID)
                           .withExternalFields(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC)
                           .build();
        final AuditRecordGeneratorStateConsumerImpl<AuditedType> auditRecordGenerator = newAuditRecordGenerator(auditedFieldSet);

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, emptyList());

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(hasNoMandatoryFieldValues()));
    }

    @Test
    public void generate_WithInternalMandatoryOnly_ShouldGenerateMandatoryFieldValuesAndCreatedFieldRecords() {
        final AuditedCommand cmd = new AuditedCommand(ID, CREATE)
            .with(AuditedType.NAME, NAME)
            .with(AuditedType.DESC, DESC);

        final CurrentEntityState currentState = CurrentEntityState.EMPTY;

        final AuditedFieldSet<AuditedType> auditedFieldSet =
            AuditedFieldSet.builder(AuditedType.ID)
                           .withInternalFields(ALWAYS, AuditedType.NAME, AuditedType.DESC)
                           .build();
        final AuditRecordGeneratorStateConsumerImpl<AuditedType> auditRecordGenerator = newAuditRecordGenerator(auditedFieldSet);

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, emptyList());

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasMandatoryFieldValue(AuditedType.NAME, NAME),
                                      hasMandatoryFieldValue(AuditedType.DESC, DESC),
                                      hasCreatedFieldRecord(AuditedType.NAME, NAME),
                                      hasCreatedFieldRecord(AuditedType.DESC, DESC))));
    }

    @Test
    public void generate_WithOnCreateOrUpdateOnly_ShouldGenerateCreatedFieldRecords() {
        final AuditedCommand cmd = new AuditedCommand(ID, CREATE)
            .with(AuditedType.NAME, NAME)
            .with(AuditedType.DESC, DESC);

        final CurrentEntityState currentState = CurrentEntityState.EMPTY;

        final AuditedFieldSet<AuditedType> auditedFieldSet =
            AuditedFieldSet.builder(AuditedType.ID)
                           .withInternalFields(ON_CREATE_OR_UPDATE, ImmutableSet.of(AuditedType.NAME, AuditedType.DESC))
                           .build();
        final AuditRecordGeneratorStateConsumerImpl<AuditedType> auditRecordGenerator = newAuditRecordGenerator(auditedFieldSet);

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, emptyList());

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasCreatedFieldRecord(AuditedType.NAME, NAME),
                                      hasCreatedFieldRecord(AuditedType.DESC, DESC))));
    }

    @Test
    public void generate_WithOnUpdateOnly_ShouldGenerateCreatedFieldRecords() {
        final AuditedCommand cmd = new AuditedCommand(ID, CREATE)
            .with(AuditedType.NAME, NAME)
            .with(AuditedType.DESC, DESC);

        final CurrentEntityState currentState = CurrentEntityState.EMPTY;

        final AuditedFieldSet<AuditedType> auditedFieldSet =
            AuditedFieldSet.builder(AuditedType.ID)
                           .withInternalFields(ON_UPDATE, ImmutableSet.of(AuditedType.NAME, AuditedType.DESC))
                           .build();
        final AuditRecordGeneratorStateConsumerImpl<AuditedType> auditRecordGenerator = newAuditRecordGenerator(auditedFieldSet);

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, emptyList());

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasCreatedFieldRecord(AuditedType.NAME, NAME),
                                      hasCreatedFieldRecord(AuditedType.DESC, DESC))));
    }

    @Test
    public void generate_WithChildRecordsOnly_ShouldGenerateChildRecords() {
        final AuditedCommand cmd = new AuditedCommand(ID, CREATE);

        final CurrentEntityState currentState = CurrentEntityState.EMPTY;

        final AuditedFieldSet<AuditedType> auditedFieldSet = AuditedFieldSet.builder(AuditedType.ID).build();
        final AuditRecordGeneratorStateConsumerImpl<AuditedType> auditRecordGenerator = newAuditRecordGenerator(auditedFieldSet);

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);

        final List<AuditRecord<?>> childRecords = ImmutableList.of(mockChildRecord(), mockChildRecord());

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, childRecords);

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasSameChildRecord(childRecords.get(0)),
                                      hasSameChildRecord(childRecords.get(1)))));
    }

    @Test
    public void generate_WithExternalAndInternalMandatoryOnly_ShouldGenerateMandatoryFieldValuesForBothTypesAndCreatedRecordsForInternal() {
        final AuditedCommand cmd = new AuditedCommand(ID, CREATE)
            .with(AuditedType.NAME, NAME)
            .with(AuditedType.DESC, DESC);

        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();
        currentState.set(NotAuditedAncestorType.NAME, ANCESTOR_NAME);
        currentState.set(NotAuditedAncestorType.DESC, ANCESTOR_DESC);

        final AuditedFieldSet<AuditedType> auditedFieldSet = AuditedFieldSet.builder(AuditedType.ID)
                                                                            .withExternalFields(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC)
                                                                            .withInternalFields(ALWAYS, AuditedType.NAME, AuditedType.DESC)
                                                                            .build();
        final AuditRecordGeneratorStateConsumerImpl<AuditedType> auditRecordGenerator = newAuditRecordGenerator(auditedFieldSet);

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, emptyList());

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasMandatoryFieldValue(NotAuditedAncestorType.NAME, ANCESTOR_NAME),
                                      hasMandatoryFieldValue(NotAuditedAncestorType.DESC, ANCESTOR_DESC),
                                      hasMandatoryFieldValue(AuditedType.NAME, NAME),
                                      hasMandatoryFieldValue(AuditedType.DESC, DESC),
                                      hasCreatedFieldRecord(AuditedType.NAME, NAME),
                                      hasCreatedFieldRecord(AuditedType.DESC, DESC))));
    }

    @Test
    public void generate_WithInternalMandatoryAndOnCreateOrUpdateOnly_ShouldGenerateMandatoryFieldValuesForMandatoryAndCreatedRecordsForAll() {
        final AuditedCommand cmd = new AuditedCommand(ID, CREATE)
            .with(AuditedType.NAME, NAME)
            .with(AuditedType.DESC, DESC)
            .with(AuditedType.DESC2, DESC2);

        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();

        final AuditedFieldSet<AuditedType> auditedFieldSet = AuditedFieldSet.builder(AuditedType.ID)
                                                                            .withInternalFields(ALWAYS, AuditedType.NAME, AuditedType.DESC)
                                                                            .withInternalFields(ON_CREATE_OR_UPDATE, AuditedType.DESC2)
                                                                            .build();
        final AuditRecordGeneratorStateConsumerImpl<AuditedType> auditRecordGenerator = newAuditRecordGenerator(auditedFieldSet);

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, emptyList());

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasMandatoryFieldValue(AuditedType.NAME, NAME),
                                      hasMandatoryFieldValue(AuditedType.DESC, DESC),
                                      hasCreatedFieldRecord(AuditedType.NAME, NAME),
                                      hasCreatedFieldRecord(AuditedType.DESC, DESC),
                                      hasCreatedFieldRecord(AuditedType.DESC2, DESC2))));
    }

    @Test
    public void generate_WithExternalMandatoryAndChildRecordsOnly_ShouldGenerateMandatoryFieldValuesAndChildRecords() {
        final AuditedCommand cmd = new AuditedCommand(ID, CREATE);

        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();
        currentState.set(NotAuditedAncestorType.NAME, ANCESTOR_NAME);
        currentState.set(NotAuditedAncestorType.DESC, ANCESTOR_DESC);

        final AuditedFieldSet<AuditedType> auditedFieldSet = AuditedFieldSet.builder(AuditedType.ID)
                                                                            .withExternalFields(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC)
                                                                            .build();
        final AuditRecordGeneratorStateConsumerImpl<AuditedType> auditRecordGenerator = newAuditRecordGenerator(auditedFieldSet);

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);

        final List<AuditRecord<?>> childRecords = ImmutableList.of(mockChildRecord(), mockChildRecord());

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, childRecords);

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasMandatoryFieldValue(NotAuditedAncestorType.NAME, ANCESTOR_NAME),
                                      hasMandatoryFieldValue(NotAuditedAncestorType.DESC, ANCESTOR_DESC),
                                      hasSameChildRecord(childRecords.get(0)),
                                      hasSameChildRecord(childRecords.get(1)))));
    }

    @Test
    public void generate_WithEverything_ShouldGenerateEverything() {
        final AuditedCommand cmd = new AuditedCommand(ID, CREATE)
            .with(AuditedType.NAME, NAME)
            .with(AuditedType.DESC, DESC)
            .with(AuditedType.DESC2, DESC2);

        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();
        currentState.set(NotAuditedAncestorType.NAME, ANCESTOR_NAME);
        currentState.set(NotAuditedAncestorType.DESC, ANCESTOR_DESC);

        final AuditedFieldSet<AuditedType> auditedFieldSet =
            AuditedFieldSet.builder(AuditedType.ID)
                           .withExternalFields(ImmutableSet.of(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC))
                           .withInternalFields(ALWAYS, AuditedType.NAME)
                           .withInternalFields(ON_CREATE_OR_UPDATE, AuditedType.DESC)
                           .withInternalFields(ON_UPDATE, AuditedType.DESC2)
                           .build();
        final AuditRecordGeneratorStateConsumerImpl<AuditedType> auditRecordGenerator = newAuditRecordGenerator(auditedFieldSet);

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);

        final List<AuditRecord<?>> childRecords = ImmutableList.of(mockChildRecord(), mockChildRecord());

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, childRecords);

        //noinspection unchecked
        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasMandatoryFieldValue(NotAuditedAncestorType.NAME, ANCESTOR_NAME),
                                      hasMandatoryFieldValue(NotAuditedAncestorType.DESC, ANCESTOR_DESC),
                                      hasMandatoryFieldValue(AuditedType.NAME, NAME),
                                      hasCreatedFieldRecord(AuditedType.NAME, NAME),
                                      hasCreatedFieldRecord(AuditedType.DESC, DESC),
                                      hasCreatedFieldRecord(AuditedType.DESC2, DESC2),
                                      hasSameChildRecord(childRecords.get(0)),
                                      hasSameChildRecord(childRecords.get(1)))));
    }

    private AuditRecord<?> mockChildRecord() {
        return mock(AuditRecord.class);
    }

    private AuditRecordGeneratorStateConsumerImpl<AuditedType> newAuditRecordGenerator(final AuditedFieldSet<AuditedType> fieldSet) {
        return new AuditRecordGeneratorStateConsumerImpl<>(fieldSet, entityIdExtractor, fieldsToFetchResolver);
    }
}