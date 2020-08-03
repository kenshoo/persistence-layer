package com.kenshoo.pl.entity.internal.audit;

import com.google.common.collect.ImmutableList;
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
import static com.kenshoo.pl.entity.ChangeOperation.DELETE;
import static com.kenshoo.pl.entity.audit.AuditTrigger.ALWAYS;
import static com.kenshoo.pl.entity.audit.AuditTrigger.ON_CREATE_OR_UPDATE;
import static com.kenshoo.pl.entity.matchers.audit.AuditRecordMatchers.*;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static org.hamcrest.CoreMatchers.allOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class AuditRecordGeneratorForDeleteTest {

    private static final long ID = 1234;
    private static final String STRING_ID = String.valueOf(ID);
    private static final String ANCESTOR_NAME = "ancestorName";
    private static final String ANCESTOR_DESC = "ancestorDesc";
    private static final String NAME = "name";
    private static final String DESC = "desc";

    @Mock
    private EntityIdExtractor entityIdExtractor;

    @Mock
    private AuditedFieldsToFetchResolver fieldsToFetchResolver;

    @Test
    public void generate_WithIdOnly_ShouldGenerateBasicData() {
        final AuditedCommand cmd = new AuditedCommand(ID, DELETE);

        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();
        currentState.set(AuditedType.NAME, "oldName");
        currentState.set(AuditedType.DESC, "oldDesc");

        final AuditedFieldSet<AuditedType> auditedFieldSet = AuditedFieldSet.builder(AuditedType.ID).build();
        final AuditRecordGenerator<AuditedType> auditRecordGenerator = newAuditRecordGenerator(auditedFieldSet);

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, emptyList());

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasEntityType(AuditedType.INSTANCE),
                                      hasEntityId(STRING_ID),
                                      hasOperator(DELETE))));
    }

    @Test
    public void generate_WithExternalMandatoryOnly_ShouldGenerateMandatoryFieldValues() {
        final AuditedCommand cmd = new AuditedCommand(ID, DELETE);

        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();
        currentState.set(NotAuditedAncestorType.NAME, ANCESTOR_NAME);
        currentState.set(NotAuditedAncestorType.DESC, ANCESTOR_DESC);

        final AuditedFieldSet<AuditedType> auditedFieldSet =
            AuditedFieldSet.builder(AuditedType.ID)
                           .withExternalFields(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC)
                           .build();
        final AuditRecordGenerator<AuditedType> auditRecordGenerator = newAuditRecordGenerator(auditedFieldSet);

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);

        final List<AuditRecord<?>> childRecords = ImmutableList.of(mockChildRecord(), mockChildRecord());

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, childRecords);

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasMandatoryFieldValue(NotAuditedAncestorType.NAME, ANCESTOR_NAME),
                                      hasMandatoryFieldValue(NotAuditedAncestorType.DESC, ANCESTOR_DESC))));
    }

    @Test
    public void generate_WithInternalMandatoryOnly_ShouldGenerateMandatoryFieldValues() {
        final AuditedCommand cmd = new AuditedCommand(ID, DELETE);

        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();
        currentState.set(AuditedType.NAME, NAME);
        currentState.set(AuditedType.DESC, DESC);

        final AuditedFieldSet<AuditedType> auditedFieldSet =
            AuditedFieldSet.builder(AuditedType.ID)
                           .withInternalFields(ALWAYS, AuditedType.NAME, AuditedType.DESC)
                           .build();
        final AuditRecordGenerator<AuditedType> auditRecordGenerator = newAuditRecordGenerator(auditedFieldSet);

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, emptyList());

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasMandatoryFieldValue(AuditedType.NAME, NAME),
                                      hasMandatoryFieldValue(AuditedType.DESC, DESC))));
    }

    @Test
    public void generate_WithChildRecordsOnly_ShouldGenerateChildRecords() {
        final AuditedCommand cmd = new AuditedCommand(ID, DELETE)
            .with(AuditedType.NAME, "name");

        final CurrentEntityState currentState = CurrentEntityState.EMPTY;

        final AuditedFieldSet<AuditedType> auditedFieldSet =
            AuditedFieldSet.builder(AuditedType.ID)
                           .withInternalFields(ON_CREATE_OR_UPDATE, singleton(AuditedType.NAME))
                           .build();
        final AuditRecordGenerator<AuditedType> auditRecordGenerator = newAuditRecordGenerator(auditedFieldSet);

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);

        final List<AuditRecord<?>> childRecords = ImmutableList.of(mockChildRecord(), mockChildRecord());

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, childRecords);

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasSameChildRecord(childRecords.get(0)),
                                      hasSameChildRecord(childRecords.get(1)))));
    }

    @Test
    public void generate_WithExternalAndInternalMandatoryOnly_ShouldGenerateMandatoryFieldValuesForBothTypes() {
        final AuditedCommand cmd = new AuditedCommand(ID, DELETE);

        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();
        currentState.set(NotAuditedAncestorType.NAME, ANCESTOR_NAME);
        currentState.set(NotAuditedAncestorType.DESC, ANCESTOR_DESC);
        currentState.set(AuditedType.NAME, NAME);
        currentState.set(AuditedType.DESC, DESC);

        final AuditedFieldSet<AuditedType> auditedFieldSet =
            AuditedFieldSet.builder(AuditedType.ID)
                           .withExternalFields(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC)
                           .withInternalFields(ALWAYS, AuditedType.NAME, AuditedType.DESC)
                           .build();
        final AuditRecordGenerator<AuditedType> auditRecordGenerator = newAuditRecordGenerator(auditedFieldSet);

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, emptyList());

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasMandatoryFieldValue(NotAuditedAncestorType.NAME, ANCESTOR_NAME),
                                      hasMandatoryFieldValue(NotAuditedAncestorType.DESC, ANCESTOR_DESC),
                                      hasMandatoryFieldValue(AuditedType.NAME, NAME),
                                      hasMandatoryFieldValue(AuditedType.DESC, DESC))));
    }

    @Test
    public void generate_WithExternalMandatoryAndChildRecords_ShouldGenerateMandatoryFieldValuesAndChildRecords() {
        final AuditedCommand cmd = new AuditedCommand(ID, DELETE)
            .with(AuditedType.NAME, "name");

        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();
        currentState.set(NotAuditedAncestorType.NAME, ANCESTOR_NAME);
        currentState.set(NotAuditedAncestorType.DESC, ANCESTOR_DESC);

        final AuditedFieldSet<AuditedType> auditedFieldSet =
            AuditedFieldSet.builder(AuditedType.ID)
                           .withExternalFields(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC)
                           .build();
        final AuditRecordGenerator<AuditedType> auditRecordGenerator = newAuditRecordGenerator(auditedFieldSet);

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

    private AuditRecord<?> mockChildRecord() {
        return mock(AuditRecord.class);
    }

    private AuditRecordGenerator<AuditedType> newAuditRecordGenerator(final AuditedFieldSet<AuditedType> fieldSet) {
        return new AuditRecordGenerator<>(fieldSet, entityIdExtractor, fieldsToFetchResolver);
    }
}