package com.kenshoo.pl.entity.internal.audit;

import com.google.common.collect.ImmutableList;
import com.kenshoo.pl.entity.AuditRecord;
import com.kenshoo.pl.entity.Entity;
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

import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAnd;
import static com.kenshoo.pl.entity.ChangeOperation.DELETE;
import static com.kenshoo.pl.entity.matchers.audit.AuditRecordMatchers.*;
import static com.kenshoo.pl.matchers.IterableStreamMatcher.eqStreamAsSet;
import static java.util.Collections.*;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.CoreMatchers.allOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AuditRecordGeneratorForDeleteTest {

    private static final long ID = 1234;
    private static final String STRING_ID = String.valueOf(ID);

    @Mock
    private AuditedFieldSet<TestAuditedEntityType> completeFieldSet;

    @Mock
    private EntityIdExtractor entityIdExtractor;

    @InjectMocks
    private AuditRecordGenerator<TestAuditedEntityType> auditRecordGenerator;

    @Test
    public void generate_WithIdOnly_ShouldGenerateFixedData() {
        final TestAuditedEntityCommand cmd = new TestAuditedEntityCommand(ID, DELETE);

        final EntityImpl entity = new EntityImpl();
        entity.set(TestAuditedEntityType.NAME, "oldName");
        entity.set(TestAuditedEntityType.DESC, "oldDesc");

        when(completeFieldSet.intersectWith(eqStreamAsSet(emptySet()))).thenReturn(new AuditedFieldSet<>(TestAuditedEntityType.ID));
        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, entity);

        final Optional<? extends AuditRecord<TestAuditedEntityType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, entity, emptyList());

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasEntityType(TestAuditedEntityType.INSTANCE),
                                      hasEntityId(STRING_ID),
                                      hasOperator(DELETE))));
    }

    @Test
    public void generate_WithIdAndChildRecords_ShouldGenerateFixedDataAndChildRecords() {
        final TestAuditedEntityCommand cmd = new TestAuditedEntityCommand(ID, DELETE)
            .with(TestAuditedEntityType.NAME, "name");
        final Set<? extends EntityField<TestAuditedEntityType, ?>> cmdChangedFields = cmd.getChangedFields().collect(toSet());

        final Entity entity = Entity.EMPTY;

        final AuditedFieldSet<TestAuditedEntityType> expectedIntersectionFieldSet =
            new AuditedFieldSet<>(TestAuditedEntityType.ID, singleton(TestAuditedEntityType.NAME));

        when(completeFieldSet.intersectWith(eqStreamAsSet(cmdChangedFields))).thenReturn(expectedIntersectionFieldSet);
        doReturn(Optional.of(STRING_ID)).when(entityIdExtractor).extract(cmd, entity);

        final List<AuditRecord<?>> childRecords = ImmutableList.of(mockChildRecord(), mockChildRecord());

        final Optional<? extends AuditRecord<TestAuditedEntityType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, entity, childRecords);

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasEntityType(TestAuditedEntityType.INSTANCE),
                                      hasEntityId(STRING_ID),
                                      hasOperator(DELETE),
                                      hasSameChildRecord(childRecords.get(0)),
                                      hasSameChildRecord(childRecords.get(1)))));
    }

    private AuditRecord<?> mockChildRecord() {
        return mock(AuditRecord.class);
    }
}