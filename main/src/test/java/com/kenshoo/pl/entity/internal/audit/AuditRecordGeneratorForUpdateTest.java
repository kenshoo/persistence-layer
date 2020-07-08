package com.kenshoo.pl.entity.internal.audit;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.kenshoo.pl.entity.CurrentEntityMutableState;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.audit.AuditRecord;
import com.kenshoo.pl.entity.internal.EntityIdExtractor;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType;
import com.kenshoo.pl.entity.internal.audit.entitytypes.NotAuditedAncestorType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAnd;
import static com.kenshoo.pl.entity.ChangeOperation.UPDATE;
import static com.kenshoo.pl.entity.matchers.audit.AuditRecordMatchers.*;
import static com.kenshoo.pl.matchers.IterableStreamMatcher.eqStreamAsSet;
import static java.util.Collections.*;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AuditRecordGeneratorForUpdateTest {

    private static final long ID = 1234;
    private static final String STRING_ID = String.valueOf(ID);
    private static final String ANCESTOR_NAME = "ancestorName";
    private static final String ANCESTOR_DESC = "ancestorDesc";

    @Mock
    private AuditedFieldSet<AuditedType> completeFieldSet;

    @Mock
    private EntityIdExtractor entityIdExtractor;

    @InjectMocks
    private AuditRecordGenerator<AuditedType> auditRecordGenerator;

    @Test
    public void generate_WithIdOnly_ShouldReturnEmpty() {
        final AuditedCommand cmd = new AuditedCommand(ID, UPDATE);

        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();
         currentState.set(AuditedType.ID, ID);

        final AuditedFieldSet<AuditedType> expectedIntersectionFieldSet =
            AuditedFieldSet.builder(AuditedType.ID).build();

        when(completeFieldSet.intersectWith(eqStreamAsSet(emptySet()))).thenReturn(expectedIntersectionFieldSet);
        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, emptyList());

        assertThat(actualOptionalAuditRecord, isEmpty());
    }

    @Test
    public void generate_WithMandatoryFieldsOnly_ShouldReturnEmpty() {
        final AuditedCommand cmd = new AuditedCommand(ID, UPDATE);

        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();
         currentState.set(AuditedType.ID, ID);
         currentState.set(NotAuditedAncestorType.NAME, ANCESTOR_NAME);
         currentState.set(NotAuditedAncestorType.DESC, ANCESTOR_DESC);

        final AuditedFieldSet<AuditedType> expectedIntersectionFieldSet =
            AuditedFieldSet.builder(AuditedType.ID).build();

        when(completeFieldSet.intersectWith(eqStreamAsSet(emptySet()))).thenReturn(expectedIntersectionFieldSet);
        //noinspection ResultOfMethodCallIgnored
        doReturn(ImmutableSet.of(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC))
            .when(completeFieldSet).getExternalMandatoryFields();
        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, emptyList());

        assertThat(actualOptionalAuditRecord, isEmpty());
    }

    @Test
    public void generate_WithOnChangeFields_AllIntersect_AllChanged_ShouldGenerateBasicDataAndFieldRecordsForAll() {
        final AuditedCommand cmd = new AuditedCommand(ID, UPDATE)
            .with(AuditedType.NAME, "newName")
            .with(AuditedType.DESC, "newDesc");
        final Set<? extends EntityField<AuditedType, ?>> cmdChangedFields = cmd.getChangedFields().collect(toSet());

        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();
         currentState.set(AuditedType.NAME, "oldName");
         currentState.set(AuditedType.DESC, "oldDesc");

        final AuditedFieldSet<AuditedType> expectedIntersectionFieldSet =
            AuditedFieldSet.builder(AuditedType.ID)
                           .withOnChangeFields(ImmutableSet.of(AuditedType.NAME, AuditedType.DESC))
                           .build();

        when(completeFieldSet.intersectWith(eqStreamAsSet(cmdChangedFields))).thenReturn(expectedIntersectionFieldSet);
        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, emptyList());

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasEntityType(AuditedType.INSTANCE),
                                      hasEntityId(STRING_ID),
                                      hasOperator(UPDATE),
                                      hasChangedFieldRecord(AuditedType.NAME, "oldName", "newName"),
                                      hasChangedFieldRecord(AuditedType.DESC, "oldDesc", "newDesc"))));
    }

    @Test
    public void generate_WithOnChangeFields_AllIntersect_SomeChanged_ShouldGenerateFieldRecordsForChanged() {
        final AuditedCommand cmd = new AuditedCommand(ID, UPDATE)
            .with(AuditedType.NAME, "newName")
            .with(AuditedType.DESC, "newDesc")
            .with(AuditedType.DESC2, "desc2");
        final Set<? extends EntityField<AuditedType, ?>> cmdChangedFields = cmd.getChangedFields().collect(toSet());

        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();
         currentState.set(AuditedType.NAME, "oldName");
         currentState.set(AuditedType.DESC, "oldDesc");
         currentState.set(AuditedType.DESC2, "desc2");

        final AuditedFieldSet<AuditedType> expectedIntersectionFieldSet =
            AuditedFieldSet.builder(AuditedType.ID)
                           .withOnChangeFields(ImmutableSet.of(AuditedType.NAME,
                                                               AuditedType.DESC,
                                                               AuditedType.DESC2))
                           .build();

        when(completeFieldSet.intersectWith(eqStreamAsSet(cmdChangedFields))).thenReturn(expectedIntersectionFieldSet);
        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, emptyList());

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasChangedFieldRecord(AuditedType.NAME, "oldName", "newName"),
                                      hasChangedFieldRecord(AuditedType.DESC, "oldDesc", "newDesc"),
                                      not(hasFieldRecordFor(AuditedType.DESC2)))));
    }

    @Test
    public void generate_WithOnChangeFields_AllIntersect_NoneChanged_ShouldReturnEmpty() {
        final AuditedCommand cmd = new AuditedCommand(ID, UPDATE)
            .with(AuditedType.NAME, "name")
            .with(AuditedType.DESC, "desc")
            .with(AuditedType.DESC2, "desc2");
        final Set<? extends EntityField<AuditedType, ?>> cmdChangedFields = cmd.getChangedFields().collect(toSet());

        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();
         currentState.set(AuditedType.NAME, "name");
         currentState.set(AuditedType.DESC, "desc");
         currentState.set(AuditedType.DESC2, "desc2");

        final AuditedFieldSet<AuditedType> expectedIntersectionFieldSet =
            AuditedFieldSet.builder(AuditedType.ID)
                           .withOnChangeFields(ImmutableSet.of(AuditedType.NAME,
                                                               AuditedType.DESC,
                                                               AuditedType.DESC2))
                           .build();

        when(completeFieldSet.intersectWith(eqStreamAsSet(cmdChangedFields))).thenReturn(expectedIntersectionFieldSet);
        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, emptyList());

        assertThat(actualOptionalAuditRecord, isEmpty());
    }

    @Test
    public void generate_WithOnChangeFields_SomeIntersect_AllChanged_ShouldGenerateRecordsForIntersectedOnly() {
        final AuditedCommand cmd = new AuditedCommand(ID, UPDATE)
            .with(AuditedType.NAME, "newName")
            .with(AuditedType.DESC, "newDesc")
            .with(AuditedType.DESC2, "desc2");
        final Set<? extends EntityField<AuditedType, ?>> cmdChangedFields = cmd.getChangedFields().collect(toSet());

        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();
         currentState.set(AuditedType.NAME, "oldName");
         currentState.set(AuditedType.DESC, "oldDesc");
         currentState.set(AuditedType.DESC2, "desc2");

        final AuditedFieldSet<AuditedType> expectedIntersectionFieldSet =
            AuditedFieldSet.builder(AuditedType.ID)
                           .withOnChangeFields(ImmutableSet.of(AuditedType.NAME,
                                                               AuditedType.DESC))
                           .build();

        when(completeFieldSet.intersectWith(eqStreamAsSet(cmdChangedFields))).thenReturn(expectedIntersectionFieldSet);
        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, emptyList());

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasChangedFieldRecord(AuditedType.NAME, "oldName", "newName"),
                                      hasChangedFieldRecord(AuditedType.DESC, "oldDesc", "newDesc"),
                                      not(hasFieldRecordFor(AuditedType.DESC2)))));
    }

    @Test
    public void generate_WithOnChangeFields_SomeIntersect_SomeChanged_ShouldGenerateRecordsForIntersectedAndChangedOnly() {
        final AuditedCommand cmd = new AuditedCommand(ID, UPDATE)
            .with(AuditedType.NAME, "newName")
            .with(AuditedType.DESC, "desc")
            .with(AuditedType.DESC2, "desc2");
        final Set<? extends EntityField<AuditedType, ?>> cmdChangedFields = cmd.getChangedFields().collect(toSet());

        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();
         currentState.set(AuditedType.NAME, "oldName");
         currentState.set(AuditedType.DESC, "desc");
         currentState.set(AuditedType.DESC2, "desc2");

        final AuditedFieldSet<AuditedType> expectedIntersectionFieldSet =
            AuditedFieldSet.builder(AuditedType.ID)
                           .withOnChangeFields(ImmutableSet.of(AuditedType.NAME,
                                                               AuditedType.DESC))
                           .build();

        when(completeFieldSet.intersectWith(eqStreamAsSet(cmdChangedFields))).thenReturn(expectedIntersectionFieldSet);
        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, emptyList());

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasChangedFieldRecord(AuditedType.NAME, "oldName", "newName"),
                                      not(hasFieldRecordFor(AuditedType.DESC)),
                                      not(hasFieldRecordFor(AuditedType.DESC2)))));
    }

    @Test
    public void generate_WithOnChangeFields_SomeIntersect_NoneChanged_ShouldReturnEmpty() {
        final AuditedCommand cmd = new AuditedCommand(ID, UPDATE)
            .with(AuditedType.NAME, "name")
            .with(AuditedType.DESC, "desc")
            .with(AuditedType.DESC2, "desc2");
        final Set<? extends EntityField<AuditedType, ?>> cmdChangedFields = cmd.getChangedFields().collect(toSet());

        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();
         currentState.set(AuditedType.NAME, "name");
         currentState.set(AuditedType.DESC, "desc");
         currentState.set(AuditedType.DESC2, "desc2");

        final AuditedFieldSet<AuditedType> expectedIntersectionFieldSet =
            AuditedFieldSet.builder(AuditedType.ID)
                           .withOnChangeFields(ImmutableSet.of(AuditedType.NAME,
                                                               AuditedType.DESC))
                           .build();

        when(completeFieldSet.intersectWith(eqStreamAsSet(cmdChangedFields))).thenReturn(expectedIntersectionFieldSet);
        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, emptyList());

        assertThat(actualOptionalAuditRecord, isEmpty());
    }

    @Test
    public void generate_WithOnChangeFields_NoneIntersect_AllChanged_ShouldReturnEmpty() {
        final AuditedCommand cmd = new AuditedCommand(ID, UPDATE)
            .with(AuditedType.NAME, "newName")
            .with(AuditedType.DESC, "newDesc");
        final Set<? extends EntityField<AuditedType, ?>> cmdChangedFields = cmd.getChangedFields().collect(toSet());

        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();
         currentState.set(AuditedType.NAME, "oldName");
         currentState.set(AuditedType.NAME, "oldDesc");

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);
        when(completeFieldSet.intersectWith(eqStreamAsSet(cmdChangedFields)))
            .thenReturn(AuditedFieldSet.builder(AuditedType.ID).build());

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, emptyList());

        assertThat(actualOptionalAuditRecord, isEmpty());
    }

    @Test
    public void generate_WhenFieldChangedFromNull_ShouldGenerateCreatedFieldRecord() {
        final AuditedCommand cmd = new AuditedCommand(ID, UPDATE)
            .with(AuditedType.NAME, "newName");

        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();
         currentState.set(AuditedType.NAME, null);

        final AuditedFieldSet<AuditedType> expectedIntersectionFieldSet =
            AuditedFieldSet.builder(AuditedType.ID)
                           .withOnChangeFields(singleton(AuditedType.NAME))
                           .build();

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);
        when(completeFieldSet.intersectWith(eqStreamAsSet(singleton(AuditedType.NAME)))).thenReturn(expectedIntersectionFieldSet);

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, emptyList());

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(hasCreatedFieldRecord(AuditedType.NAME, "newName")));
    }

    @Test
    public void generate_WhenFieldChangedToNull_ShouldGenerateDeletedFieldRecord() {
        final AuditedCommand cmd = new AuditedCommand(ID, UPDATE)
            .with(AuditedType.NAME, null);

        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();
         currentState.set(AuditedType.NAME, "oldName");

        final AuditedFieldSet<AuditedType> expectedIntersectionFieldSet =
            AuditedFieldSet.builder(AuditedType.ID)
                           .withOnChangeFields(singleton(AuditedType.NAME))
                           .build();

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);
        when(completeFieldSet.intersectWith(eqStreamAsSet(singleton(AuditedType.NAME)))).thenReturn(expectedIntersectionFieldSet);

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, emptyList());

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(hasDeletedFieldRecord(AuditedType.NAME, "oldName")));
    }

    @Test
    public void generate_WithOnChangeFields_AndChildRecords_ShouldGenerateFieldRecordsAndChildRecords() {
        final AuditedCommand cmd = new AuditedCommand(ID, UPDATE)
            .with(AuditedType.NAME, "newName")
            .with(AuditedType.DESC, "newDesc");
        final Set<? extends EntityField<AuditedType, ?>> cmdChangedFields = cmd.getChangedFields().collect(toSet());

        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();
         currentState.set(AuditedType.NAME, "oldName");
         currentState.set(AuditedType.DESC, "oldDesc");

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);
        when(completeFieldSet.intersectWith(eqStreamAsSet(cmdChangedFields)))
            .thenReturn(AuditedFieldSet.builder(AuditedType.ID)
                                       .withOnChangeFields(ImmutableSet.of(AuditedType.NAME,
                                                                           AuditedType.DESC))
                                       .build());

        final List<AuditRecord<?>> childRecords = ImmutableList.of(mockChildRecord(), mockChildRecord());

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, childRecords);

        //noinspection unchecked
        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasEntityType(AuditedType.INSTANCE),
                                      hasEntityId(STRING_ID),
                                      hasOperator(UPDATE),
                                      hasChangedFieldRecord(AuditedType.NAME, "oldName", "newName"),
                                      hasChangedFieldRecord(AuditedType.DESC, "oldDesc", "newDesc"),
                                      hasSameChildRecord(childRecords.get(0)),
                                      hasSameChildRecord(childRecords.get(1)))));
    }

    @Test
    public void generate_WithIdAndChildRecordsOnly_ShouldGenerateBasicDataAndChildRecords() {
        final AuditedCommand cmd = new AuditedCommand(ID, UPDATE);

        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();
         currentState.set(AuditedType.ID, ID);

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);
        when(completeFieldSet.intersectWith(eqStreamAsSet(emptySet())))
            .thenReturn(AuditedFieldSet.builder(AuditedType.ID).build());

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
    public void generate_WithMandatoryFieldsAndChildRecordsOnly_ShouldGenerateBasicAndMandatoryFieldsAndChildRecords() {
        final AuditedCommand cmd = new AuditedCommand(ID, UPDATE);

        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();
         currentState.set(AuditedType.ID, ID);
         currentState.set(NotAuditedAncestorType.NAME, ANCESTOR_NAME);
         currentState.set(NotAuditedAncestorType.DESC, ANCESTOR_DESC);

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);
        when(completeFieldSet.intersectWith(eqStreamAsSet(emptySet())))
            .thenReturn(AuditedFieldSet.builder(AuditedType.ID).build());
        //noinspection ResultOfMethodCallIgnored
        doReturn(ImmutableSet.of(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC))
            .when(completeFieldSet).getExternalMandatoryFields();

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
            .with(AuditedType.NAME, "newName")
            .with(AuditedType.DESC, "newDesc");
        final Set<? extends EntityField<AuditedType, ?>> cmdChangedFields = cmd.getChangedFields().collect(toSet());

        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();
         currentState.set(AuditedType.ID, ID);
         currentState.set(AuditedType.NAME, "oldName");
         currentState.set(AuditedType.DESC, "oldDesc");
         currentState.set(NotAuditedAncestorType.NAME, ANCESTOR_NAME);
         currentState.set(NotAuditedAncestorType.DESC, ANCESTOR_DESC);

        final AuditedFieldSet<AuditedType> expectedIntersectionFieldSet =
            AuditedFieldSet.builder(AuditedType.ID)
                           .withOnChangeFields(ImmutableSet.of(AuditedType.NAME, AuditedType.DESC))
                           .build();

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, currentState);
        when(completeFieldSet.intersectWith(eqStreamAsSet(cmdChangedFields)))
            .thenReturn(expectedIntersectionFieldSet);
        //noinspection ResultOfMethodCallIgnored
        doReturn(ImmutableSet.of(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC))
            .when(completeFieldSet).getExternalMandatoryFields();

        final List<AuditRecord<?>> childRecords = ImmutableList.of(mockChildRecord(), mockChildRecord());

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, currentState, childRecords);

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasMandatoryFieldValue(NotAuditedAncestorType.NAME, ANCESTOR_NAME),
                                      hasMandatoryFieldValue(NotAuditedAncestorType.DESC, ANCESTOR_DESC),
                                      hasChangedFieldRecord(AuditedType.NAME, "oldName", "newName"),
                                      hasChangedFieldRecord(AuditedType.DESC, "oldDesc", "newDesc"),
                                      hasSameChildRecord(childRecords.get(0)),
                                      hasSameChildRecord(childRecords.get(1)))));
    }

    private AuditRecord<?> mockChildRecord() {
        return mock(AuditRecord.class);
    }
}