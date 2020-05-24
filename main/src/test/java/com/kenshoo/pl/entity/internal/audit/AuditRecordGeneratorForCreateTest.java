package com.kenshoo.pl.entity.internal.audit;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.kenshoo.pl.entity.Entity;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.audit.AuditRecord;
import com.kenshoo.pl.entity.internal.EntityIdExtractor;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAnd;
import static com.kenshoo.pl.entity.ChangeOperation.CREATE;
import static com.kenshoo.pl.entity.matchers.audit.AuditRecordMatchers.*;
import static com.kenshoo.pl.matchers.IterableStreamMatcher.eqStreamAsSet;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.CoreMatchers.allOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AuditRecordGeneratorForCreateTest {

    private static final long ID = 1234;
    private static final String STRING_ID = String.valueOf(ID);

    @Mock
    private AuditedFieldSet<AuditedType> completeFieldSet;

    @Mock
    private EntityIdExtractor entityIdExtractor;

    @InjectMocks
    private AuditRecordGenerator<AuditedType> auditRecordGenerator;

    @Test
    public void generate_WithIdOnly_ShouldGenerateMandatoryData() {
        final AuditedCommand cmd = new AuditedCommand(ID, CREATE);

        final Entity entity = Entity.EMPTY;

        final AuditedFieldSet<AuditedType> expectedIntersectionFieldSet = AuditedFieldSet.builder(AuditedType.ID).build();

        when(completeFieldSet.intersectWith(eqStreamAsSet(emptySet()))).thenReturn(expectedIntersectionFieldSet);
        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, entity);

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, entity, emptyList());

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasEntityType(AuditedType.INSTANCE),
                                      hasEntityId(STRING_ID),
                                      hasOperator(CREATE))));
    }

    @Test
    public void generate_WithIdAndOnChangeFieldsOnly_ShouldGenerateMandatoryDataAndFieldRecords() {
        final AuditedCommand cmd = new AuditedCommand(ID, CREATE)
            .with(AuditedType.NAME, "name")
            .with(AuditedType.DESC, "desc");
        final Set<? extends EntityField<AuditedType, ?>> cmdChangedFields = cmd.getChangedFields().collect(toSet());

        final Entity entity = Entity.EMPTY;

        final AuditedFieldSet<AuditedType> expectedIntersectionFieldSet =
            AuditedFieldSet.builder(AuditedType.ID)
                           .withOnChangeFields(ImmutableSet.of(AuditedType.NAME, AuditedType.DESC))
                           .build();

        when(completeFieldSet.intersectWith(eqStreamAsSet(cmdChangedFields))).thenReturn(expectedIntersectionFieldSet);
        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, entity);

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, entity, emptyList());

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasEntityType(AuditedType.INSTANCE),
                                      hasEntityId(STRING_ID),
                                      hasOperator(CREATE),
                                      hasCreatedFieldRecord(AuditedType.NAME, "name"),
                                      hasCreatedFieldRecord(AuditedType.DESC, "desc"))));
    }

    @Test
    public void generate_WithIdAndChildRecordsOnly_ShouldGenerateMandatoryDataAndChildRecords() {
        final AuditedCommand cmd = new AuditedCommand(ID, CREATE);

        final Entity entity = Entity.EMPTY;

        final AuditedFieldSet<AuditedType> expectedIntersectionFieldSet = AuditedFieldSet.builder(AuditedType.ID).build();

        when(completeFieldSet.intersectWith(eqStreamAsSet(emptySet()))).thenReturn(expectedIntersectionFieldSet);
        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, entity);

        final List<AuditRecord<?>> childRecords = ImmutableList.of(mockChildRecord(), mockChildRecord());

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, entity, childRecords);

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasEntityType(AuditedType.INSTANCE),
                                      hasEntityId(STRING_ID),
                                      hasOperator(CREATE),
                                      hasSameChildRecord(childRecords.get(0)),
                                      hasSameChildRecord(childRecords.get(1)))));
    }

    @Test
    public void generate_WithEverything_ShouldGenerateEverything() {
        final AuditedCommand cmd = new AuditedCommand(ID, CREATE)
            .with(AuditedType.NAME, "name")
            .with(AuditedType.DESC, "desc");
        final Set<? extends EntityField<AuditedType, ?>> cmdChangedFields = cmd.getChangedFields().collect(toSet());

        final Entity entity = Entity.EMPTY;

        final AuditedFieldSet<AuditedType> expectedIntersectionFieldSet =
            AuditedFieldSet.builder(AuditedType.ID)
                           .withOnChangeFields(ImmutableSet.of(AuditedType.NAME, AuditedType.DESC))
                           .build();

        when(completeFieldSet.intersectWith(eqStreamAsSet(cmdChangedFields))).thenReturn(expectedIntersectionFieldSet);
        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, entity);

        final List<AuditRecord<?>> childRecords = ImmutableList.of(mockChildRecord(), mockChildRecord());

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, entity, childRecords);

        //noinspection unchecked
        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasEntityType(AuditedType.INSTANCE),
                                      hasEntityId(STRING_ID),
                                      hasOperator(CREATE),
                                      hasCreatedFieldRecord(AuditedType.NAME, "name"),
                                      hasCreatedFieldRecord(AuditedType.DESC, "desc"),
                                      hasSameChildRecord(childRecords.get(0)),
                                      hasSameChildRecord(childRecords.get(1)))));
    }

    private AuditRecord<?> mockChildRecord() {
        return mock(AuditRecord.class);
    }
}