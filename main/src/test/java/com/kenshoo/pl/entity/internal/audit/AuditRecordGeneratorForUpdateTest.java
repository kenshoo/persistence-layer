package com.kenshoo.pl.entity.internal.audit;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.kenshoo.pl.entity.AuditRecord;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.internal.EntityIdExtractor;
import com.kenshoo.pl.entity.internal.EntityImpl;
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

    @Mock
    private AuditedFieldSet<TestAuditedEntityType> completeFieldSet;

    @Mock
    private EntityIdExtractor entityIdExtractor;

    @InjectMocks
    private AuditRecordGenerator<TestAuditedEntityType> auditRecordGenerator;

    @Test
    public void generate_WithIdOnly_ShouldReturnEmpty() {
        final TestAuditedEntityCommand cmd = new TestAuditedEntityCommand(ID, UPDATE);

        final EntityImpl entity = new EntityImpl();
        entity.set(TestAuditedEntityType.ID, ID);

        final AuditedFieldSet<TestAuditedEntityType> expectedIntersectionFieldSet =
            new AuditedFieldSet<>(TestAuditedEntityType.ID);

        when(completeFieldSet.intersectWith(eqStreamAsSet(emptySet()))).thenReturn(expectedIntersectionFieldSet);
        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, entity);

        final Optional<? extends AuditRecord<TestAuditedEntityType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, entity, emptyList());

        assertThat(actualOptionalAuditRecord, isEmpty());
    }

    @Test
    public void generate_WithDataFields_AllIntersect_AllChanged_ShouldGenerateFixedDataAndFieldRecordsForAll() {
        final TestAuditedEntityCommand cmd = new TestAuditedEntityCommand(ID, UPDATE)
            .with(TestAuditedEntityType.NAME, "newName")
            .with(TestAuditedEntityType.DESC, "newDesc");
        final Set<? extends EntityField<TestAuditedEntityType, ?>> cmdChangedFields = cmd.getChangedFields().collect(toSet());

        final EntityImpl entity = new EntityImpl();
        entity.set(TestAuditedEntityType.NAME, "oldName");
        entity.set(TestAuditedEntityType.DESC, "oldDesc");

        final AuditedFieldSet<TestAuditedEntityType> expectedIntersectionFieldSet =
            new AuditedFieldSet<>(TestAuditedEntityType.ID,
                                  ImmutableSet.of(TestAuditedEntityType.NAME, TestAuditedEntityType.DESC));

        when(completeFieldSet.intersectWith(eqStreamAsSet(cmdChangedFields))).thenReturn(expectedIntersectionFieldSet);
        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, entity);

        final Optional<? extends AuditRecord<TestAuditedEntityType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, entity, emptyList());

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasEntityType(TestAuditedEntityType.INSTANCE),
                                      hasEntityId(STRING_ID),
                                      hasOperator(UPDATE),
                                      hasChangedFieldRecord(TestAuditedEntityType.NAME, "oldName", "newName"),
                                      hasChangedFieldRecord(TestAuditedEntityType.DESC, "oldDesc", "newDesc"))));
    }

    @Test
    public void generate_WithDataFields_AllIntersect_SomeChanged_ShouldGenerateFieldRecordsForChanged() {
        final TestAuditedEntityCommand cmd = new TestAuditedEntityCommand(ID, UPDATE)
            .with(TestAuditedEntityType.NAME, "newName")
            .with(TestAuditedEntityType.DESC, "newDesc")
            .with(TestAuditedEntityType.DESC2, "desc2");
        final Set<? extends EntityField<TestAuditedEntityType, ?>> cmdChangedFields = cmd.getChangedFields().collect(toSet());

        final EntityImpl entity = new EntityImpl();
        entity.set(TestAuditedEntityType.NAME, "oldName");
        entity.set(TestAuditedEntityType.DESC, "oldDesc");
        entity.set(TestAuditedEntityType.DESC2, "desc2");

        final AuditedFieldSet<TestAuditedEntityType> expectedIntersectionFieldSet =
            new AuditedFieldSet<>(TestAuditedEntityType.ID,
                                  ImmutableSet.of(TestAuditedEntityType.NAME,
                                                  TestAuditedEntityType.DESC,
                                                  TestAuditedEntityType.DESC2));

        when(completeFieldSet.intersectWith(eqStreamAsSet(cmdChangedFields))).thenReturn(expectedIntersectionFieldSet);
        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, entity);

        final Optional<? extends AuditRecord<TestAuditedEntityType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, entity, emptyList());

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasChangedFieldRecord(TestAuditedEntityType.NAME, "oldName", "newName"),
                                      hasChangedFieldRecord(TestAuditedEntityType.DESC, "oldDesc", "newDesc"),
                                      not(hasFieldRecordFor(TestAuditedEntityType.DESC2)))));
    }

    @Test
    public void generate_WithDataFields_AllIntersect_NoneChanged_ShouldReturnEmpty() {
        final TestAuditedEntityCommand cmd = new TestAuditedEntityCommand(ID, UPDATE)
            .with(TestAuditedEntityType.NAME, "name")
            .with(TestAuditedEntityType.DESC, "desc")
            .with(TestAuditedEntityType.DESC2, "desc2");
        final Set<? extends EntityField<TestAuditedEntityType, ?>> cmdChangedFields = cmd.getChangedFields().collect(toSet());

        final EntityImpl entity = new EntityImpl();
        entity.set(TestAuditedEntityType.NAME, "name");
        entity.set(TestAuditedEntityType.DESC, "desc");
        entity.set(TestAuditedEntityType.DESC2, "desc2");

        final AuditedFieldSet<TestAuditedEntityType> expectedIntersectionFieldSet =
            new AuditedFieldSet<>(TestAuditedEntityType.ID,
                                  ImmutableSet.of(TestAuditedEntityType.NAME,
                                                  TestAuditedEntityType.DESC,
                                                  TestAuditedEntityType.DESC2));

        when(completeFieldSet.intersectWith(eqStreamAsSet(cmdChangedFields))).thenReturn(expectedIntersectionFieldSet);
        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, entity);

        final Optional<? extends AuditRecord<TestAuditedEntityType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, entity, emptyList());

        assertThat(actualOptionalAuditRecord, isEmpty());
    }

    @Test
    public void generate_WithDataFields_SomeIntersect_AllChanged_ShouldGenerateRecordsForIntersectedOnly() {
        final TestAuditedEntityCommand cmd = new TestAuditedEntityCommand(ID, UPDATE)
            .with(TestAuditedEntityType.NAME, "newName")
            .with(TestAuditedEntityType.DESC, "newDesc")
            .with(TestAuditedEntityType.DESC2, "desc2");
        final Set<? extends EntityField<TestAuditedEntityType, ?>> cmdChangedFields = cmd.getChangedFields().collect(toSet());

        final EntityImpl entity = new EntityImpl();
        entity.set(TestAuditedEntityType.NAME, "oldName");
        entity.set(TestAuditedEntityType.DESC, "oldDesc");
        entity.set(TestAuditedEntityType.DESC2, "desc2");

        final AuditedFieldSet<TestAuditedEntityType> expectedIntersectionFieldSet =
            new AuditedFieldSet<>(TestAuditedEntityType.ID,
                                  ImmutableSet.of(TestAuditedEntityType.NAME,
                                                  TestAuditedEntityType.DESC));

        when(completeFieldSet.intersectWith(eqStreamAsSet(cmdChangedFields))).thenReturn(expectedIntersectionFieldSet);
        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, entity);

        final Optional<? extends AuditRecord<TestAuditedEntityType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, entity, emptyList());

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasChangedFieldRecord(TestAuditedEntityType.NAME, "oldName", "newName"),
                                      hasChangedFieldRecord(TestAuditedEntityType.DESC, "oldDesc", "newDesc"),
                                      not(hasFieldRecordFor(TestAuditedEntityType.DESC2)))));
    }

    @Test
    public void generate_WithDataFields_SomeIntersect_SomeChanged_ShouldGenerateRecordsForIntersectedAndChangedOnly() {
        final TestAuditedEntityCommand cmd = new TestAuditedEntityCommand(ID, UPDATE)
            .with(TestAuditedEntityType.NAME, "newName")
            .with(TestAuditedEntityType.DESC, "desc")
            .with(TestAuditedEntityType.DESC2, "desc2");
        final Set<? extends EntityField<TestAuditedEntityType, ?>> cmdChangedFields = cmd.getChangedFields().collect(toSet());

        final EntityImpl entity = new EntityImpl();
        entity.set(TestAuditedEntityType.NAME, "oldName");
        entity.set(TestAuditedEntityType.DESC, "desc");
        entity.set(TestAuditedEntityType.DESC2, "desc2");

        final AuditedFieldSet<TestAuditedEntityType> expectedIntersectionFieldSet =
            new AuditedFieldSet<>(TestAuditedEntityType.ID,
                                  ImmutableSet.of(TestAuditedEntityType.NAME,
                                                  TestAuditedEntityType.DESC));

        when(completeFieldSet.intersectWith(eqStreamAsSet(cmdChangedFields))).thenReturn(expectedIntersectionFieldSet);
        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, entity);

        final Optional<? extends AuditRecord<TestAuditedEntityType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, entity, emptyList());

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasChangedFieldRecord(TestAuditedEntityType.NAME, "oldName", "newName"),
                                      not(hasFieldRecordFor(TestAuditedEntityType.DESC)),
                                      not(hasFieldRecordFor(TestAuditedEntityType.DESC2)))));
    }

    @Test
    public void generate_WithDataFields_SomeIntersect_NoneChanged_ShouldReturnEmpty() {
        final TestAuditedEntityCommand cmd = new TestAuditedEntityCommand(ID, UPDATE)
            .with(TestAuditedEntityType.NAME, "name")
            .with(TestAuditedEntityType.DESC, "desc")
            .with(TestAuditedEntityType.DESC2, "desc2");
        final Set<? extends EntityField<TestAuditedEntityType, ?>> cmdChangedFields = cmd.getChangedFields().collect(toSet());

        final EntityImpl entity = new EntityImpl();
        entity.set(TestAuditedEntityType.NAME, "name");
        entity.set(TestAuditedEntityType.DESC, "desc");
        entity.set(TestAuditedEntityType.DESC2, "desc2");

        final AuditedFieldSet<TestAuditedEntityType> expectedIntersectionFieldSet =
            new AuditedFieldSet<>(TestAuditedEntityType.ID,
                                  ImmutableSet.of(TestAuditedEntityType.NAME,
                                                  TestAuditedEntityType.DESC));

        when(completeFieldSet.intersectWith(eqStreamAsSet(cmdChangedFields))).thenReturn(expectedIntersectionFieldSet);
        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, entity);

        final Optional<? extends AuditRecord<TestAuditedEntityType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, entity, emptyList());

        assertThat(actualOptionalAuditRecord, isEmpty());
    }

    @Test
    public void generate_WithDataFields_NoneIntersect_AllChanged_ShouldReturnEmpty() {
        final TestAuditedEntityCommand cmd = new TestAuditedEntityCommand(ID, UPDATE)
            .with(TestAuditedEntityType.NAME, "newName")
            .with(TestAuditedEntityType.DESC, "newDesc");
        final Set<? extends EntityField<TestAuditedEntityType, ?>> cmdChangedFields = cmd.getChangedFields().collect(toSet());

        final EntityImpl entity = new EntityImpl();
        entity.set(TestAuditedEntityType.NAME, "oldName");
        entity.set(TestAuditedEntityType.NAME, "oldDesc");

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, entity);
        when(completeFieldSet.intersectWith(eqStreamAsSet(cmdChangedFields))).thenReturn(new AuditedFieldSet<>(TestAuditedEntityType.ID));

        final Optional<? extends AuditRecord<TestAuditedEntityType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, entity, emptyList());

        assertThat(actualOptionalAuditRecord, isEmpty());
    }

    @Test
    public void generate_WhenFieldChangedFromNull_ShouldGenerateCreatedFieldRecord() {
        final TestAuditedEntityCommand cmd = new TestAuditedEntityCommand(ID, UPDATE)
            .with(TestAuditedEntityType.NAME, "newName");

        final EntityImpl entity = new EntityImpl();
        entity.set(TestAuditedEntityType.NAME, null);

        final AuditedFieldSet<TestAuditedEntityType> expectedIntersectionFieldSet =
            new AuditedFieldSet<>(TestAuditedEntityType.ID,
                                  singleton(TestAuditedEntityType.NAME));

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, entity);
        when(completeFieldSet.intersectWith(eqStreamAsSet(singleton(TestAuditedEntityType.NAME)))).thenReturn(expectedIntersectionFieldSet);

        final Optional<? extends AuditRecord<TestAuditedEntityType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, entity, emptyList());

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(hasCreatedFieldRecord(TestAuditedEntityType.NAME, "newName")));
    }

    @Test
    public void generate_WhenFieldChangedToNull_ShouldGenerateDeletedFieldRecord() {
        final TestAuditedEntityCommand cmd = new TestAuditedEntityCommand(ID, UPDATE)
            .with(TestAuditedEntityType.NAME, null);

        final EntityImpl entity = new EntityImpl();
        entity.set(TestAuditedEntityType.NAME, "oldName");

        final AuditedFieldSet<TestAuditedEntityType> expectedIntersectionFieldSet =
            new AuditedFieldSet<>(TestAuditedEntityType.ID,
                                  singleton(TestAuditedEntityType.NAME));

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, entity);
        when(completeFieldSet.intersectWith(eqStreamAsSet(singleton(TestAuditedEntityType.NAME)))).thenReturn(expectedIntersectionFieldSet);

        final Optional<? extends AuditRecord<TestAuditedEntityType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, entity, emptyList());

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(hasDeletedFieldRecord(TestAuditedEntityType.NAME, "oldName")));
    }

    @Test
    public void generate_WithDataFields_AllIntersected_AllChanged_AndChildRecords_ShouldGenerateEverything() {
        final TestAuditedEntityCommand cmd = new TestAuditedEntityCommand(ID, UPDATE)
            .with(TestAuditedEntityType.NAME, "newName")
            .with(TestAuditedEntityType.DESC, "newDesc");
        final Set<? extends EntityField<TestAuditedEntityType, ?>> cmdChangedFields = cmd.getChangedFields().collect(toSet());

        final EntityImpl entity = new EntityImpl();
        entity.set(TestAuditedEntityType.NAME, "oldName");
        entity.set(TestAuditedEntityType.DESC, "oldDesc");

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, entity);
        when(completeFieldSet.intersectWith(eqStreamAsSet(cmdChangedFields)))
            .thenReturn(new AuditedFieldSet<>(TestAuditedEntityType.ID,
                                              ImmutableSet.of(TestAuditedEntityType.NAME,
                                                              TestAuditedEntityType.DESC)));

        final List<AuditRecord<?>> childRecords = ImmutableList.of(mockChildRecord(), mockChildRecord());

        final Optional<? extends AuditRecord<TestAuditedEntityType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, entity, childRecords);

        //noinspection unchecked
        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasEntityType(TestAuditedEntityType.INSTANCE),
                                      hasEntityId(STRING_ID),
                                      hasOperator(UPDATE),
                                      hasChangedFieldRecord(TestAuditedEntityType.NAME, "oldName", "newName"),
                                      hasChangedFieldRecord(TestAuditedEntityType.DESC, "oldDesc", "newDesc"),
                                      hasSameChildRecord(childRecords.get(0)),
                                      hasSameChildRecord(childRecords.get(1)))));
    }

    @Test
    public void generate_WithIdAndChildRecordsOnly_ShouldGenerateFixedDataAndChildRecords() {
        final TestAuditedEntityCommand cmd = new TestAuditedEntityCommand(ID, UPDATE);

        final EntityImpl entity = new EntityImpl();
        entity.set(TestAuditedEntityType.ID, ID);

        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, entity);
        when(completeFieldSet.intersectWith(eqStreamAsSet(emptySet())))
            .thenReturn(new AuditedFieldSet<>(TestAuditedEntityType.ID));

        final List<AuditRecord<?>> childRecords = ImmutableList.of(mockChildRecord(), mockChildRecord());

        final Optional<? extends AuditRecord<TestAuditedEntityType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, entity, childRecords);

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasEntityType(TestAuditedEntityType.INSTANCE),
                                      hasEntityId(STRING_ID),
                                      hasOperator(UPDATE),
                                      hasSameChildRecord(childRecords.get(0)),
                                      hasSameChildRecord(childRecords.get(1)))));
    }

    private AuditRecord<?> mockChildRecord() {
        return mock(AuditRecord.class);
    }
}