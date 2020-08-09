package com.kenshoo.pl.entity.internal.audit;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.kenshoo.pl.entity.CurrentEntityMutableState;
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

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAnd;
import static com.kenshoo.pl.entity.ChangeOperation.UPDATE;
import static com.kenshoo.pl.entity.audit.AuditTrigger.*;
import static com.kenshoo.pl.entity.matchers.audit.AuditRecordMatchers.*;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class AuditRecordGeneratorImplForUpdateTest {

    private static final long ID = 1234;
    private static final String STRING_ID = String.valueOf(ID);
    private static final String ANCESTOR_NAME = "ancestorName";
    private static final String ANCESTOR_DESC = "ancestorDesc";
    private static final String NEW_NAME = "newName";
    private static final String OLD_NAME = "oldName";
    private static final String NEW_DESC = "newDesc";
    private static final String OLD_DESC = "oldDesc";
    private static final String NEW_DESC2 = "newDesc2";
    private static final String OLD_DESC2 = "oldDesc2";

    @Mock
    private EntityIdExtractor entityIdExtractor;

    @Test
    public void generate_WithIdOnly_ShouldReturnEmpty() {
        final AuditedCommand cmd = new AuditedCommand(ID, UPDATE);

        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();
        currentState.set(AuditedType.ID, ID);

        final AuditedFieldSet<AuditedType> auditedFieldSet = AuditedFieldSet.builder(AuditedType.ID).build();
        final AuditRecordGeneratorImpl<AuditedType> auditRecordGenerator = newAuditRecordGenerator(auditedFieldSet);

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, emptyList());

        assertThat(actualOptionalAuditRecord, isEmpty());
    }

    @Test
    public void generate_WithExternalMandatoryOnly_ShouldReturnEmpty() {
        final AuditedCommand cmd = new AuditedCommand(ID, UPDATE);

        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();
        currentState.set(AuditedType.ID, ID);
        currentState.set(NotAuditedAncestorType.NAME, ANCESTOR_NAME);
        currentState.set(NotAuditedAncestorType.DESC, ANCESTOR_DESC);

        final AuditedFieldSet<AuditedType> auditedFieldSet =
            AuditedFieldSet.builder(AuditedType.ID)
                           .withExternalFields(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC)
                           .build();
        final AuditRecordGeneratorImpl<AuditedType> auditRecordGenerator = newAuditRecordGenerator(auditedFieldSet);

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, emptyList());

        assertThat(actualOptionalAuditRecord, isEmpty());
    }

    @Test
    public void generate_WithInternalMandatoryOnly_AllIntersect_AllChanged_ShouldCreateMandatoryFieldValuesAndFieldRecordsForAll() {
        final AuditedCommand cmd = new AuditedCommand(ID, UPDATE)
            .with(AuditedType.NAME, NEW_NAME)
            .with(AuditedType.DESC, NEW_DESC);

        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();
        currentState.set(AuditedType.ID, ID);
        currentState.set(AuditedType.NAME, OLD_NAME);
        currentState.set(AuditedType.DESC, OLD_DESC);

        final AuditedFieldSet<AuditedType> auditedFieldSet =
            AuditedFieldSet.builder(AuditedType.ID)
                           .withInternalFields(ALWAYS, AuditedType.NAME, AuditedType.DESC)
                           .build();
        final AuditRecordGeneratorImpl<AuditedType> auditRecordGenerator = newAuditRecordGenerator(auditedFieldSet);

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, emptyList());

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasMandatoryFieldValue(AuditedType.NAME, NEW_NAME),
                                      hasMandatoryFieldValue(AuditedType.DESC, NEW_DESC),
                                      hasChangedFieldRecord(AuditedType.NAME, OLD_NAME, NEW_NAME),
                                      hasChangedFieldRecord(AuditedType.DESC, OLD_DESC, NEW_DESC))));
    }

    @Test
    public void generate_WithInternalMandatoryOnly_AllIntersect_SomeChanged_ShouldCreateFieldRecordsForChangedOnly() {
        final AuditedCommand cmd = new AuditedCommand(ID, UPDATE)
            .with(AuditedType.NAME, NEW_NAME)
            .with(AuditedType.DESC, OLD_DESC);

        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();
        currentState.set(AuditedType.ID, ID);
        currentState.set(AuditedType.NAME, OLD_NAME);
        currentState.set(AuditedType.DESC, OLD_DESC);

        final AuditedFieldSet<AuditedType> auditedFieldSet =
            AuditedFieldSet.builder(AuditedType.ID)
                           .withInternalFields(ALWAYS, AuditedType.NAME, AuditedType.DESC)
                           .build();
        final AuditRecordGeneratorImpl<AuditedType> auditRecordGenerator = newAuditRecordGenerator(auditedFieldSet);

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, emptyList());

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasChangedFieldRecord(AuditedType.NAME, OLD_NAME, NEW_NAME),
                                      not(hasFieldRecordFor(AuditedType.DESC)))));
    }

    @Test
    public void generate_WithInternalMandatoryOnly_AllIntersect_NoneChanged_ShouldReturnEmpty() {
        final AuditedCommand cmd = new AuditedCommand(ID, UPDATE)
            .with(AuditedType.NAME, OLD_NAME)
            .with(AuditedType.DESC, OLD_DESC);

        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();
        currentState.set(AuditedType.ID, ID);
        currentState.set(AuditedType.NAME, OLD_NAME);
        currentState.set(AuditedType.DESC, OLD_DESC);

        final AuditedFieldSet<AuditedType> auditedFieldSet =
            AuditedFieldSet.builder(AuditedType.ID)
                           .withInternalFields(ALWAYS, AuditedType.NAME, AuditedType.DESC)
                           .build();
        final AuditRecordGeneratorImpl<AuditedType> auditRecordGenerator = newAuditRecordGenerator(auditedFieldSet);

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, emptyList());

        assertThat(actualOptionalAuditRecord, isEmpty());
    }

    @Test
    public void generate_WithInternalMandatoryOnly_SomeIntersect_AllChanged_ShouldCreateFieldRecordsForIntersectionOnly() {
        final AuditedCommand cmd = new AuditedCommand(ID, UPDATE)
            .with(AuditedType.NAME, NEW_NAME)
            .with(AuditedType.DESC, NEW_DESC);

        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();
        currentState.set(AuditedType.ID, ID);
        currentState.set(AuditedType.NAME, OLD_NAME);
        currentState.set(AuditedType.DESC, OLD_DESC);

        final AuditedFieldSet<AuditedType> auditedFieldSet =
            AuditedFieldSet.builder(AuditedType.ID)
                           .withInternalFields(ALWAYS, AuditedType.NAME)
                           .build();
        final AuditRecordGeneratorImpl<AuditedType> auditRecordGenerator = newAuditRecordGenerator(auditedFieldSet);

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, emptyList());

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasChangedFieldRecord(AuditedType.NAME, OLD_NAME, NEW_NAME),
                                      not(hasFieldRecordFor(AuditedType.DESC)))));
    }

    @Test
    public void generate_WithInternalMandatoryOnly_SomeIntersect_SomeChanged_ShouldCreateFieldRecordsForBothChangedAndIntersectedOnly() {
        final AuditedCommand cmd = new AuditedCommand(ID, UPDATE)
            .with(AuditedType.NAME, NEW_NAME)
            .with(AuditedType.DESC, OLD_DESC)
            .with(AuditedType.DESC2, OLD_DESC2);

        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();
        currentState.set(AuditedType.ID, ID);
        currentState.set(AuditedType.NAME, OLD_NAME);
        currentState.set(AuditedType.DESC, OLD_DESC);

        final AuditedFieldSet<AuditedType> auditedFieldSet =
            AuditedFieldSet.builder(AuditedType.ID)
                           .withInternalFields(ALWAYS, AuditedType.NAME, AuditedType.DESC)
                           .build();
        final AuditRecordGeneratorImpl<AuditedType> auditRecordGenerator = newAuditRecordGenerator(auditedFieldSet);

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, emptyList());

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasChangedFieldRecord(AuditedType.NAME, OLD_NAME, NEW_NAME),
                                      not(hasFieldRecordFor(AuditedType.DESC)),
                                      not(hasFieldRecordFor(AuditedType.DESC2)))));
    }

    @Test
    public void generate_WithInternalMandatoryOnly_NoneIntersect_SomeChanged_ShouldReturnEmpty() {
        final AuditedCommand cmd = new AuditedCommand(ID, UPDATE)
            .with(AuditedType.DESC, NEW_DESC)
            .with(AuditedType.DESC2, NEW_DESC2);

        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();
        currentState.set(AuditedType.ID, ID);
        currentState.set(AuditedType.NAME, OLD_NAME);
        currentState.set(AuditedType.DESC, OLD_DESC);
        currentState.set(AuditedType.DESC2, OLD_DESC2);

        final AuditedFieldSet<AuditedType> auditedFieldSet =
            AuditedFieldSet.builder(AuditedType.ID)
                           .withInternalFields(ALWAYS, AuditedType.NAME)
                           .build();
        final AuditRecordGeneratorImpl<AuditedType> auditRecordGenerator = newAuditRecordGenerator(auditedFieldSet);

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, emptyList());

        assertThat(actualOptionalAuditRecord, isEmpty());
    }

    @Test
    public void generate_WithOnCreateOrUpdate_AllIntersect_AllChanged_ShouldGenerateBasicDataAndFieldRecordsForAll() {
        final AuditedCommand cmd = new AuditedCommand(ID, UPDATE)
            .with(AuditedType.NAME, NEW_NAME)
            .with(AuditedType.DESC, NEW_DESC);

        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();
        currentState.set(AuditedType.NAME, OLD_NAME);
        currentState.set(AuditedType.DESC, OLD_DESC);

        final AuditedFieldSet<AuditedType> auditedFieldSet =
            AuditedFieldSet.builder(AuditedType.ID)
                           .withInternalFields(ON_CREATE_OR_UPDATE, ImmutableSet.of(AuditedType.NAME, AuditedType.DESC))
                           .build();
        final AuditRecordGeneratorImpl<AuditedType> auditRecordGenerator = newAuditRecordGenerator(auditedFieldSet);

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, emptyList());

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasEntityType(AuditedType.INSTANCE),
                                      hasEntityId(STRING_ID),
                                      hasOperator(UPDATE),
                                      hasChangedFieldRecord(AuditedType.NAME, OLD_NAME, NEW_NAME),
                                      hasChangedFieldRecord(AuditedType.DESC, OLD_DESC, NEW_DESC))));
    }

    @Test
    public void generate_WithOnCreateOrUpdate_AllIntersect_SomeChanged_ShouldGenerateFieldRecordsForChanged() {
        final AuditedCommand cmd = new AuditedCommand(ID, UPDATE)
            .with(AuditedType.NAME, NEW_NAME)
            .with(AuditedType.DESC, NEW_DESC)
            .with(AuditedType.DESC2, OLD_DESC2);

        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();
        currentState.set(AuditedType.NAME, OLD_NAME);
        currentState.set(AuditedType.DESC, OLD_DESC);
        currentState.set(AuditedType.DESC2, OLD_DESC2);

        final AuditedFieldSet<AuditedType> auditedFieldSet =
            AuditedFieldSet.builder(AuditedType.ID)
                           .withInternalFields(ON_CREATE_OR_UPDATE,
                                               AuditedType.NAME, AuditedType.DESC, AuditedType.DESC2)
                           .build();
        final AuditRecordGeneratorImpl<AuditedType> auditRecordGenerator = newAuditRecordGenerator(auditedFieldSet);

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, emptyList());

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasChangedFieldRecord(AuditedType.NAME, OLD_NAME, NEW_NAME),
                                      hasChangedFieldRecord(AuditedType.DESC, OLD_DESC, NEW_DESC),
                                      not(hasFieldRecordFor(AuditedType.DESC2)))));
    }

    @Test
    public void generate_WithOnCreateOrUpdate_AllIntersect_NoneChanged_ShouldReturnEmpty() {
        final AuditedCommand cmd = new AuditedCommand(ID, UPDATE)
            .with(AuditedType.NAME, OLD_NAME)
            .with(AuditedType.DESC, OLD_DESC)
            .with(AuditedType.DESC2, OLD_DESC2);

        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();
        currentState.set(AuditedType.NAME, OLD_NAME);
        currentState.set(AuditedType.DESC, OLD_DESC);
        currentState.set(AuditedType.DESC2, OLD_DESC2);

        final AuditedFieldSet<AuditedType> auditedFieldSet =
            AuditedFieldSet.builder(AuditedType.ID)
                           .withInternalFields(ON_CREATE_OR_UPDATE,
                                               AuditedType.NAME, AuditedType.DESC, AuditedType.DESC2)
                           .build();
        final AuditRecordGeneratorImpl<AuditedType> auditRecordGenerator = newAuditRecordGenerator(auditedFieldSet);

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, emptyList());

        assertThat(actualOptionalAuditRecord, isEmpty());
    }

    @Test
    public void generate_WithOnCreateOrUpdate_SomeIntersect_AllChanged_ShouldGenerateRecordsForIntersectedOnly() {
        final AuditedCommand cmd = new AuditedCommand(ID, UPDATE)
            .with(AuditedType.NAME, NEW_NAME)
            .with(AuditedType.DESC, NEW_DESC)
            .with(AuditedType.DESC2, NEW_DESC2);

        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();
        currentState.set(AuditedType.NAME, OLD_NAME);
        currentState.set(AuditedType.DESC, OLD_DESC);
        currentState.set(AuditedType.DESC2, OLD_DESC2);

        final AuditedFieldSet<AuditedType> auditedFieldSet =
            AuditedFieldSet.builder(AuditedType.ID)
                           .withInternalFields(ON_CREATE_OR_UPDATE, AuditedType.NAME, AuditedType.DESC)
                           .build();
        final AuditRecordGeneratorImpl<AuditedType> auditRecordGenerator = newAuditRecordGenerator(auditedFieldSet);

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, emptyList());

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasChangedFieldRecord(AuditedType.NAME, OLD_NAME, NEW_NAME),
                                      hasChangedFieldRecord(AuditedType.DESC, OLD_DESC, NEW_DESC),
                                      not(hasFieldRecordFor(AuditedType.DESC2)))));
    }

    @Test
    public void generate_WithOnCreateOrUpdate_SomeIntersect_SomeChanged_ShouldGenerateRecordsForIntersectedAndChangedOnly() {
        final AuditedCommand cmd = new AuditedCommand(ID, UPDATE)
            .with(AuditedType.NAME, NEW_NAME)
            .with(AuditedType.DESC, OLD_DESC)
            .with(AuditedType.DESC2, OLD_DESC2);

        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();
        currentState.set(AuditedType.NAME, OLD_NAME);
        currentState.set(AuditedType.DESC, OLD_DESC);
        currentState.set(AuditedType.DESC2, OLD_DESC2);

        final AuditedFieldSet<AuditedType> auditedFieldSet =
            AuditedFieldSet.builder(AuditedType.ID)
                           .withInternalFields(ON_CREATE_OR_UPDATE, AuditedType.NAME, AuditedType.DESC)
                           .build();
        final AuditRecordGeneratorImpl<AuditedType> auditRecordGenerator = newAuditRecordGenerator(auditedFieldSet);

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, emptyList());

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasChangedFieldRecord(AuditedType.NAME, OLD_NAME, NEW_NAME),
                                      not(hasFieldRecordFor(AuditedType.DESC)),
                                      not(hasFieldRecordFor(AuditedType.DESC2)))));
    }

    @Test
    public void generate_WithOnCreateOrUpdate_SomeIntersect_NoneChanged_ShouldReturnEmpty() {
        final AuditedCommand cmd = new AuditedCommand(ID, UPDATE)
            .with(AuditedType.NAME, OLD_NAME)
            .with(AuditedType.DESC, OLD_DESC)
            .with(AuditedType.DESC2, OLD_DESC2);

        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();
        currentState.set(AuditedType.NAME, OLD_NAME);
        currentState.set(AuditedType.DESC, OLD_DESC);
        currentState.set(AuditedType.DESC2, OLD_DESC2);

        final AuditedFieldSet<AuditedType> auditedFieldSet =
            AuditedFieldSet.builder(AuditedType.ID)
                           .withInternalFields(ON_CREATE_OR_UPDATE, AuditedType.NAME, AuditedType.DESC)
                           .build();
        final AuditRecordGeneratorImpl<AuditedType> auditRecordGenerator = newAuditRecordGenerator(auditedFieldSet);

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, emptyList());

        assertThat(actualOptionalAuditRecord, isEmpty());
    }

    @Test
    public void generate_WithOnCreateOrUpdate_NoneIntersect_AllChanged_ShouldReturnEmpty() {
        final AuditedCommand cmd = new AuditedCommand(ID, UPDATE)
            .with(AuditedType.NAME, NEW_NAME)
            .with(AuditedType.DESC, NEW_DESC);

        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();
        currentState.set(AuditedType.NAME, OLD_NAME);
        currentState.set(AuditedType.DESC, OLD_DESC);

        final AuditedFieldSet<AuditedType> auditedFieldSet = AuditedFieldSet.builder(AuditedType.ID).build();
        final AuditRecordGeneratorImpl<AuditedType> auditRecordGenerator = newAuditRecordGenerator(auditedFieldSet);

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, emptyList());

        assertThat(actualOptionalAuditRecord, isEmpty());
    }

    @Test
    public void generate_WithOnUpdate_AllIntersect_AllChanged_ShouldGenerateBasicDataAndFieldRecordsForAll() {
        final AuditedCommand cmd = new AuditedCommand(ID, UPDATE)
            .with(AuditedType.NAME, NEW_NAME)
            .with(AuditedType.DESC, NEW_DESC);

        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();
        currentState.set(AuditedType.NAME, OLD_NAME);
        currentState.set(AuditedType.DESC, OLD_DESC);

        final AuditedFieldSet<AuditedType> auditedFieldSet =
            AuditedFieldSet.builder(AuditedType.ID)
                           .withInternalFields(ON_UPDATE, AuditedType.NAME, AuditedType.DESC)
                           .build();
        final AuditRecordGeneratorImpl<AuditedType> auditRecordGenerator = newAuditRecordGenerator(auditedFieldSet);

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, emptyList());

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasEntityType(AuditedType.INSTANCE),
                                      hasEntityId(STRING_ID),
                                      hasOperator(UPDATE),
                                      hasChangedFieldRecord(AuditedType.NAME, OLD_NAME, NEW_NAME),
                                      hasChangedFieldRecord(AuditedType.DESC, OLD_DESC, NEW_DESC))));
    }

    @Test
    public void generate_WithOnUpdate_AllIntersect_SomeChanged_ShouldGenerateFieldRecordsForChanged() {
        final AuditedCommand cmd = new AuditedCommand(ID, UPDATE)
            .with(AuditedType.NAME, NEW_NAME)
            .with(AuditedType.DESC, NEW_DESC)
            .with(AuditedType.DESC2, OLD_DESC2);

        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();
        currentState.set(AuditedType.NAME, OLD_NAME);
        currentState.set(AuditedType.DESC, OLD_DESC);
        currentState.set(AuditedType.DESC2, OLD_DESC2);

        final AuditedFieldSet<AuditedType> auditedFieldSet =
            AuditedFieldSet.builder(AuditedType.ID)
                           .withInternalFields(ON_UPDATE,
                                               AuditedType.NAME, AuditedType.DESC, AuditedType.DESC2)
                           .build();
        final AuditRecordGeneratorImpl<AuditedType> auditRecordGenerator = newAuditRecordGenerator(auditedFieldSet);

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, emptyList());

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasChangedFieldRecord(AuditedType.NAME, OLD_NAME, NEW_NAME),
                                      hasChangedFieldRecord(AuditedType.DESC, OLD_DESC, NEW_DESC),
                                      not(hasFieldRecordFor(AuditedType.DESC2)))));
    }

    @Test
    public void generate_WithOnUpdate_AllIntersect_NoneChanged_ShouldReturnEmpty() {
        final AuditedCommand cmd = new AuditedCommand(ID, UPDATE)
            .with(AuditedType.NAME, OLD_NAME)
            .with(AuditedType.DESC, OLD_DESC)
            .with(AuditedType.DESC2, OLD_DESC2);

        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();
        currentState.set(AuditedType.NAME, OLD_NAME);
        currentState.set(AuditedType.DESC, OLD_DESC);
        currentState.set(AuditedType.DESC2, OLD_DESC2);

        final AuditedFieldSet<AuditedType> auditedFieldSet =
            AuditedFieldSet.builder(AuditedType.ID)
                           .withInternalFields(ON_UPDATE,
                                               AuditedType.NAME, AuditedType.DESC, AuditedType.DESC2)
                           .build();
        final AuditRecordGeneratorImpl<AuditedType> auditRecordGenerator = newAuditRecordGenerator(auditedFieldSet);

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, emptyList());

        assertThat(actualOptionalAuditRecord, isEmpty());
    }

    @Test
    public void generate_WithOnUpdate_SomeIntersect_AllChanged_ShouldGenerateRecordsForIntersectedOnly() {
        final AuditedCommand cmd = new AuditedCommand(ID, UPDATE)
            .with(AuditedType.NAME, NEW_NAME)
            .with(AuditedType.DESC, NEW_DESC)
            .with(AuditedType.DESC2, NEW_DESC2);

        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();
        currentState.set(AuditedType.NAME, OLD_NAME);
        currentState.set(AuditedType.DESC, OLD_DESC);
        currentState.set(AuditedType.DESC2, OLD_DESC2);

        final AuditedFieldSet<AuditedType> auditedFieldSet =
            AuditedFieldSet.builder(AuditedType.ID)
                           .withInternalFields(ON_UPDATE, AuditedType.NAME, AuditedType.DESC)
                           .build();
        final AuditRecordGeneratorImpl<AuditedType> auditRecordGenerator = newAuditRecordGenerator(auditedFieldSet);

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, emptyList());

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasChangedFieldRecord(AuditedType.NAME, OLD_NAME, NEW_NAME),
                                      hasChangedFieldRecord(AuditedType.DESC, OLD_DESC, NEW_DESC),
                                      not(hasFieldRecordFor(AuditedType.DESC2)))));
    }

    @Test
    public void generate_WithOnUpdate_SomeIntersect_SomeChanged_ShouldGenerateRecordsForIntersectedAndChangedOnly() {
        final AuditedCommand cmd = new AuditedCommand(ID, UPDATE)
            .with(AuditedType.NAME, NEW_NAME)
            .with(AuditedType.DESC, OLD_DESC)
            .with(AuditedType.DESC2, OLD_DESC2);

        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();
        currentState.set(AuditedType.NAME, OLD_NAME);
        currentState.set(AuditedType.DESC, OLD_DESC);
        currentState.set(AuditedType.DESC2, OLD_DESC2);

        final AuditedFieldSet<AuditedType> auditedFieldSet =
            AuditedFieldSet.builder(AuditedType.ID)
                           .withInternalFields(ON_UPDATE, AuditedType.NAME, AuditedType.DESC)
                           .build();
        final AuditRecordGeneratorImpl<AuditedType> auditRecordGenerator = newAuditRecordGenerator(auditedFieldSet);

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, emptyList());

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasChangedFieldRecord(AuditedType.NAME, OLD_NAME, NEW_NAME),
                                      not(hasFieldRecordFor(AuditedType.DESC)),
                                      not(hasFieldRecordFor(AuditedType.DESC2)))));
    }

    @Test
    public void generate_WithOnUpdate_SomeIntersect_NoneChanged_ShouldReturnEmpty() {
        final AuditedCommand cmd = new AuditedCommand(ID, UPDATE)
            .with(AuditedType.NAME, OLD_NAME)
            .with(AuditedType.DESC, OLD_DESC)
            .with(AuditedType.DESC2, OLD_DESC2);

        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();
        currentState.set(AuditedType.NAME, OLD_NAME);
        currentState.set(AuditedType.DESC, OLD_DESC);
        currentState.set(AuditedType.DESC2, OLD_DESC2);

        final AuditedFieldSet<AuditedType> auditedFieldSet =
            AuditedFieldSet.builder(AuditedType.ID)
                           .withInternalFields(ON_UPDATE, AuditedType.NAME, AuditedType.DESC)
                           .build();
        final AuditRecordGeneratorImpl<AuditedType> auditRecordGenerator = newAuditRecordGenerator(auditedFieldSet);

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, emptyList());

        assertThat(actualOptionalAuditRecord, isEmpty());
    }

    @Test
    public void generate_WithOnUpdate_NoneIntersect_AllChanged_ShouldReturnEmpty() {
        final AuditedCommand cmd = new AuditedCommand(ID, UPDATE)
            .with(AuditedType.NAME, NEW_NAME)
            .with(AuditedType.DESC, NEW_DESC);

        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();
        currentState.set(AuditedType.NAME, OLD_NAME);
        currentState.set(AuditedType.DESC, OLD_DESC);

        final AuditedFieldSet<AuditedType> auditedFieldSet = AuditedFieldSet.builder(AuditedType.ID).build();
        final AuditRecordGeneratorImpl<AuditedType> auditRecordGenerator = newAuditRecordGenerator(auditedFieldSet);

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, emptyList());

        assertThat(actualOptionalAuditRecord, isEmpty());
    }

    @Test
    public void generate_WhenFieldChangedFromNull_ShouldGenerateCreatedFieldRecord() {
        final AuditedCommand cmd = new AuditedCommand(ID, UPDATE)
            .with(AuditedType.NAME, NEW_NAME);

        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();
        currentState.set(AuditedType.NAME, null);

        final AuditedFieldSet<AuditedType> auditedFieldSet =
            AuditedFieldSet.builder(AuditedType.ID)
                           .withInternalFields(ON_UPDATE, AuditedType.NAME)
                           .build();
        final AuditRecordGeneratorImpl<AuditedType> auditRecordGenerator = newAuditRecordGenerator(auditedFieldSet);

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, emptyList());

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(hasCreatedFieldRecord(AuditedType.NAME, NEW_NAME)));
    }

    @Test
    public void generate_WhenFieldChangedToNull_ShouldGenerateDeletedFieldRecord() {
        final AuditedCommand cmd = new AuditedCommand(ID, UPDATE)
            .with(AuditedType.NAME, null);

        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();
        currentState.set(AuditedType.NAME, OLD_NAME);

        final AuditedFieldSet<AuditedType> auditedFieldSet =
            AuditedFieldSet.builder(AuditedType.ID)
                           .withInternalFields(ON_CREATE_OR_UPDATE, AuditedType.NAME)
                           .build();
        final AuditRecordGeneratorImpl<AuditedType> auditRecordGenerator = newAuditRecordGenerator(auditedFieldSet);

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, emptyList());

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(hasDeletedFieldRecord(AuditedType.NAME, OLD_NAME)));
    }

    @Test
    public void generate_WithOnCreateOrUpdate_AndChildRecords_ShouldGenerateFieldRecordsAndChildRecords() {
        final AuditedCommand cmd = new AuditedCommand(ID, UPDATE)
            .with(AuditedType.NAME, NEW_NAME)
            .with(AuditedType.DESC, NEW_DESC);

        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();
        currentState.set(AuditedType.NAME, OLD_NAME);
        currentState.set(AuditedType.DESC, OLD_DESC);

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);

        final AuditedFieldSet<AuditedType> auditedFieldSet =
            AuditedFieldSet.builder(AuditedType.ID)
                           .withInternalFields(ON_CREATE_OR_UPDATE, AuditedType.NAME, AuditedType.DESC)
                           .build();
        final AuditRecordGeneratorImpl<AuditedType> auditRecordGenerator = newAuditRecordGenerator(auditedFieldSet);

        final List<AuditRecord<?>> childRecords = ImmutableList.of(mockChildRecord(), mockChildRecord());

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, childRecords);

        //noinspection unchecked
        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasEntityType(AuditedType.INSTANCE),
                                      hasEntityId(STRING_ID),
                                      hasOperator(UPDATE),
                                      hasChangedFieldRecord(AuditedType.NAME, OLD_NAME, NEW_NAME),
                                      hasChangedFieldRecord(AuditedType.DESC, OLD_DESC, NEW_DESC),
                                      hasSameChildRecord(childRecords.get(0)),
                                      hasSameChildRecord(childRecords.get(1)))));
    }

    @Test
    public void generate_WithIdAndChildRecordsOnly_ShouldGenerateBasicDataAndChildRecords() {
        final AuditedCommand cmd = new AuditedCommand(ID, UPDATE);

        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();
        currentState.set(AuditedType.ID, ID);

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);

        final AuditedFieldSet<AuditedType> auditedFieldSet = AuditedFieldSet.builder(AuditedType.ID).build();
        final AuditRecordGeneratorImpl<AuditedType> auditRecordGenerator = newAuditRecordGenerator(auditedFieldSet);

        final List<AuditRecord<?>> childRecords = ImmutableList.of(mockChildRecord(), mockChildRecord());

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, childRecords);

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasEntityType(AuditedType.INSTANCE),
                                      hasEntityId(STRING_ID),
                                      hasOperator(UPDATE),
                                      hasSameChildRecord(childRecords.get(0)),
                                      hasSameChildRecord(childRecords.get(1)))));
    }

    @Test
    public void generate_WithExternalMandatoryAndChildRecordsOnly_ShouldGenerateBasicAndMandatoryFieldsAndChildRecords() {
        final AuditedCommand cmd = new AuditedCommand(ID, UPDATE);

        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();
        currentState.set(AuditedType.ID, ID);
        currentState.set(NotAuditedAncestorType.NAME, ANCESTOR_NAME);
        currentState.set(NotAuditedAncestorType.DESC, ANCESTOR_DESC);

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);
        final AuditedFieldSet<AuditedType> auditedFieldSet = AuditedFieldSet.builder(AuditedType.ID)
                                                                            .withExternalFields(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC)
                                                                            .build();
        final AuditRecordGeneratorImpl<AuditedType> auditRecordGenerator = newAuditRecordGenerator(auditedFieldSet);

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
        final AuditedCommand cmd = new AuditedCommand(ID, UPDATE)
            .with(AuditedType.NAME, NEW_NAME)
            .with(AuditedType.DESC, NEW_DESC)
            .with(AuditedType.DESC2, NEW_DESC2);

        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();
        currentState.set(AuditedType.ID, ID);
        currentState.set(AuditedType.NAME, OLD_NAME);
        currentState.set(AuditedType.DESC, OLD_DESC);
        currentState.set(AuditedType.DESC2, OLD_DESC2);
        currentState.set(NotAuditedAncestorType.NAME, ANCESTOR_NAME);
        currentState.set(NotAuditedAncestorType.DESC, ANCESTOR_DESC);

        final AuditedFieldSet<AuditedType> auditedFieldSet =
            AuditedFieldSet.builder(AuditedType.ID)
                           .withExternalFields(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC)
                           .withInternalFields(ALWAYS, AuditedType.NAME)
                           .withInternalFields(ON_CREATE_OR_UPDATE, AuditedType.DESC)
                           .withInternalFields(ON_UPDATE, AuditedType.DESC2)
                           .build();
        final AuditRecordGeneratorImpl<AuditedType> auditRecordGenerator = newAuditRecordGenerator(auditedFieldSet);

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);

        final List<AuditRecord<?>> childRecords = ImmutableList.of(mockChildRecord(), mockChildRecord());

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, childRecords);

        //noinspection unchecked
        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasMandatoryFieldValue(NotAuditedAncestorType.NAME, ANCESTOR_NAME),
                                      hasMandatoryFieldValue(NotAuditedAncestorType.DESC, ANCESTOR_DESC),
                                      hasMandatoryFieldValue(AuditedType.NAME, NEW_NAME),
                                      hasChangedFieldRecord(AuditedType.NAME, OLD_NAME, NEW_NAME),
                                      hasChangedFieldRecord(AuditedType.DESC, OLD_DESC, NEW_DESC),
                                      hasChangedFieldRecord(AuditedType.DESC2, OLD_DESC2, NEW_DESC2),
                                      hasSameChildRecord(childRecords.get(0)),
                                      hasSameChildRecord(childRecords.get(1)))));
    }

    private AuditRecord<?> mockChildRecord() {
        return mock(AuditRecord.class);
    }

    private AuditRecordGeneratorImpl<AuditedType> newAuditRecordGenerator(final AuditedFieldSet<AuditedType> fieldSet) {
        return new AuditRecordGeneratorImpl<>(fieldSet, entityIdExtractor);
    }
}