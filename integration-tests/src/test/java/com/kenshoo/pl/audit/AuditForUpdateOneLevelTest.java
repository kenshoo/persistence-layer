package com.kenshoo.pl.audit;

import com.google.common.collect.ImmutableList;
import com.kenshoo.jooq.DataTableUtils;
import com.kenshoo.jooq.TestJooqConfig;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.audit.TestEntityTable;
import com.kenshoo.pl.entity.internal.audit.*;
import org.jooq.DSLContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.stream.Stream;

import static com.kenshoo.pl.entity.ChangeOperation.UPDATE;
import static com.kenshoo.pl.entity.matchers.audit.AuditRecordMatchers.*;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class AuditForUpdateOneLevelTest {

    private static final long ID_1 = 1L;
    private static final long ID_2 = 2L;
    private static final long INVALID_ID = 999L;

    private PLContext plContext;
    private InMemoryAuditRecordPublisher auditRecordPublisher;

    private ChangeFlowConfig<TestAuditedEntityType> auditedEntityConfig;
    private ChangeFlowConfig<TestEntityWithAuditedFieldsType> entityWithAuditedFieldsConfig;
    private ChangeFlowConfig<TestAuditedEntityWithoutDataFieldsType> auditedEntityWithoutDataFieldsConfig;
    private ChangeFlowConfig<TestEntityType> notAuditedEntityConfig;

    private PersistenceLayer<TestAuditedEntityType> auditedEntityPL;
    private PersistenceLayer<TestEntityWithAuditedFieldsType> entityWithAuditedFieldsPL;
    private PersistenceLayer<TestAuditedEntityWithoutDataFieldsType> auditedEntityWithoutDataFieldsPL;
    private PersistenceLayer<TestEntityType> notAuditedEntityPL;

    @Before
    public void setUp() {
        final DSLContext dslContext = TestJooqConfig.create();
        auditRecordPublisher = new InMemoryAuditRecordPublisher();
        plContext = new PLContext.Builder(dslContext)
            .withFeaturePredicate(__ -> true)
            .withAuditRecordPublisher(auditRecordPublisher)
            .build();

        auditedEntityConfig = flowConfig(TestAuditedEntityType.INSTANCE);
        entityWithAuditedFieldsConfig = flowConfig(TestEntityWithAuditedFieldsType.INSTANCE);
        auditedEntityWithoutDataFieldsConfig = flowConfig(TestAuditedEntityWithoutDataFieldsType.INSTANCE);
        notAuditedEntityConfig = flowConfig(TestEntityType.INSTANCE);

        auditedEntityPL = persistenceLayer();
        entityWithAuditedFieldsPL = persistenceLayer();
        auditedEntityWithoutDataFieldsPL = persistenceLayer();
        notAuditedEntityPL = persistenceLayer();

        Stream.of(TestEntityTable.INSTANCE)
              .forEach(table -> DataTableUtils.createTable(dslContext, table));

        dslContext.insertInto(TestEntityTable.INSTANCE)
                  .set(TestEntityTable.INSTANCE.id, ID_1)
                  .set(TestEntityTable.INSTANCE.name, "nameA")
                  .set(TestEntityTable.INSTANCE.desc, "descA")
                  .set(TestEntityTable.INSTANCE.desc2, "desc2A")
                  .execute();
        dslContext.insertInto(TestEntityTable.INSTANCE)
                  .set(TestEntityTable.INSTANCE.id, ID_2)
                  .set(TestEntityTable.INSTANCE.name, "nameB")
                  .set(TestEntityTable.INSTANCE.desc, "descB")
                  .set(TestEntityTable.INSTANCE.desc2, "desc2B")
                  .execute();
    }

    @After
    public void tearDown() {
        Stream.of(TestEntityTable.INSTANCE)
              .forEach(table -> plContext.dslContext().dropTable(table).execute());
    }

    @Test
    public void oneAuditedEntity_AllFieldsChanged_ShouldCreateFieldRecordsForAll() {
        auditedEntityPL.update(singletonList(new UpdateTestAuditedEntityCommand(ID_1)
                                                 .with(TestAuditedEntityType.NAME, "newNameA")
                                                 .with(TestAuditedEntityType.DESC, "newDescA")
                                                 .with(TestAuditedEntityType.DESC2, "newDesc2A")),
                               auditedEntityConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<TestAuditedEntityType> auditRecord = typed(auditRecords.get(0));
        assertThat(auditRecord, allOf(hasEntityType(TestAuditedEntityType.INSTANCE),
                                      hasEntityId(String.valueOf(ID_1)),
                                      hasOperator(UPDATE),
                                      hasChangedFieldRecord(TestAuditedEntityType.NAME, "nameA", "newNameA"),
                                      hasChangedFieldRecord(TestAuditedEntityType.DESC, "descA", "newDescA"),
                                      hasChangedFieldRecord(TestAuditedEntityType.DESC2, "desc2A", "newDesc2A")));
    }

    @Test
    public void oneAuditedEntity_AllFieldsInCmd_SomeFieldsChanged_ShouldCreateFieldRecordsForChangedOnly() {
        auditedEntityPL.update(singletonList(new UpdateTestAuditedEntityCommand(ID_1)
                                                 .with(TestAuditedEntityType.NAME, "newNameA")
                                                 .with(TestAuditedEntityType.DESC, "newDescA")
                                                 .with(TestAuditedEntityType.DESC2, "desc2A")),
                               auditedEntityConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<TestAuditedEntityType> auditRecord = typed(auditRecords.get(0));
        assertThat(auditRecord, allOf(hasEntityType(TestAuditedEntityType.INSTANCE),
                                      hasEntityId(String.valueOf(ID_1)),
                                      hasOperator(UPDATE),
                                      hasChangedFieldRecord(TestAuditedEntityType.NAME, "nameA", "newNameA"),
                                      hasChangedFieldRecord(TestAuditedEntityType.DESC, "descA", "newDescA"),
                                      not(hasFieldRecordFor(TestAuditedEntityType.DESC2))));
    }

    @Test
    public void oneAuditedEntity_AllFieldsInCmd_NoFieldsChanged_ShouldReturnEmpty() {
        auditedEntityPL.update(singletonList(new UpdateTestAuditedEntityCommand(ID_1)
                                                 .with(TestAuditedEntityType.NAME, "nameA")
                                                 .with(TestAuditedEntityType.DESC, "descA")
                                                 .with(TestAuditedEntityType.DESC2, "desc2A")),
                               auditedEntityConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat(auditRecords, is(empty()));
    }

    @Test
    public void oneAuditedEntity_SomeFieldsInCmd_AllThoseChanged_ShouldCreateFieldRecordsForThem() {
        auditedEntityPL.update(singletonList(new UpdateTestAuditedEntityCommand(ID_1)
                                                 .with(TestAuditedEntityType.NAME, "newNameA")
                                                 .with(TestAuditedEntityType.DESC, "newDescA")),
                               auditedEntityConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<TestAuditedEntityType> auditRecord = typed(auditRecords.get(0));
        assertThat(auditRecord, allOf(hasEntityType(TestAuditedEntityType.INSTANCE),
                                      hasEntityId(String.valueOf(ID_1)),
                                      hasOperator(UPDATE),
                                      hasChangedFieldRecord(TestAuditedEntityType.NAME, "nameA", "newNameA"),
                                      hasChangedFieldRecord(TestAuditedEntityType.DESC, "descA", "newDescA"),
                                      not(hasFieldRecordFor(TestAuditedEntityType.DESC2))));
    }

    @Test
    public void oneAuditedEntity_DoesntExist_ShouldReturnEmpty() {
        auditedEntityPL.update(singletonList(new UpdateTestAuditedEntityCommand(INVALID_ID)),
                               auditedEntityConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat(auditRecords, is(empty()));
    }

    @Test
    public void twoAuditedEntities_AllFieldsChanged_ShouldCreateFieldRecordsForAll() {
        final List<UpdateTestAuditedEntityCommand> cmds =
            ImmutableList.of(new UpdateTestAuditedEntityCommand(ID_1)
                                 .with(TestAuditedEntityType.NAME, "newNameA")
                                 .with(TestAuditedEntityType.DESC, "newDescA")
                                 .with(TestAuditedEntityType.DESC2, "newDesc2A"),
                             new UpdateTestAuditedEntityCommand(ID_2)
                                 .with(TestAuditedEntityType.NAME, "newNameB")
                                 .with(TestAuditedEntityType.DESC, "newDescB")
                                 .with(TestAuditedEntityType.DESC2, "newDesc2B"));

        auditedEntityPL.update(cmds, auditedEntityConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(2));

        final AuditRecord<TestAuditedEntityType> auditRecord1 = typed(auditRecords.get(0));
        assertThat(auditRecord1, allOf(hasEntityType(TestAuditedEntityType.INSTANCE),
                                       hasEntityId(String.valueOf(ID_1)),
                                       hasOperator(UPDATE),
                                       hasChangedFieldRecord(TestAuditedEntityType.NAME, "nameA", "newNameA"),
                                       hasChangedFieldRecord(TestAuditedEntityType.DESC, "descA", "newDescA"),
                                       hasChangedFieldRecord(TestAuditedEntityType.DESC2, "desc2A", "newDesc2A")));

        final AuditRecord<TestAuditedEntityType> auditRecord2 = typed(auditRecords.get(1));
        assertThat(auditRecord2, allOf(hasEntityType(TestAuditedEntityType.INSTANCE),
                                       hasEntityId(String.valueOf(ID_2)),
                                       hasOperator(UPDATE),
                                       hasChangedFieldRecord(TestAuditedEntityType.NAME, "nameB", "newNameB"),
                                       hasChangedFieldRecord(TestAuditedEntityType.DESC, "descB", "newDescB"),
                                       hasChangedFieldRecord(TestAuditedEntityType.DESC2, "desc2B", "newDesc2B")));
    }

    @Test
    public void twoAuditedEntities_OnlyOneExists_ShouldCreateOnlyOneRecord() {
        final List<UpdateTestAuditedEntityCommand> cmds =
            ImmutableList.of(new UpdateTestAuditedEntityCommand(ID_1)
                                 .with(TestAuditedEntityType.NAME, "newNameA"),
                             new UpdateTestAuditedEntityCommand(INVALID_ID)
                                 .with(TestAuditedEntityType.NAME, "newNameB"));

        auditedEntityPL.update(cmds, auditedEntityConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));

        final AuditRecord<TestAuditedEntityType> auditRecord1 = typed(auditRecords.get(0));
        assertThat(auditRecord1, allOf(hasEntityType(TestAuditedEntityType.INSTANCE),
                                       hasEntityId(String.valueOf(ID_1)),
                                       hasOperator(UPDATE)));
    }

    @Test
    public void oneEntityWithAuditedFields_AllFieldsChanged_ShouldCreateFieldRecordsForAuditedOnly() {
        entityWithAuditedFieldsPL.update(singletonList(new UpdateTestEntityWithAuditedFieldsCommand(ID_1)
                                                 .with(TestEntityWithAuditedFieldsType.NAME, "newNameA")
                                                 .with(TestEntityWithAuditedFieldsType.DESC, "newDescA")
                                                 .with(TestEntityWithAuditedFieldsType.DESC2, "newDesc2A")),
                               entityWithAuditedFieldsConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<TestEntityWithAuditedFieldsType> auditRecord = typed(auditRecords.get(0));
        assertThat(auditRecord, allOf(hasEntityType(TestEntityWithAuditedFieldsType.INSTANCE),
                                      hasEntityId(String.valueOf(ID_1)),
                                      hasOperator(UPDATE),
                                      hasChangedFieldRecord(TestEntityWithAuditedFieldsType.NAME, "nameA", "newNameA"),
                                      hasChangedFieldRecord(TestEntityWithAuditedFieldsType.DESC, "descA", "newDescA"),
                                      not(hasFieldRecordFor(TestEntityWithAuditedFieldsType.DESC2))));
    }

    @Test
    public void oneEntityWithAuditedFields_PartiallyIntersectCmd_FieldsChanged_ShouldCreateFieldRecordsForIntersectionOnly() {
        entityWithAuditedFieldsPL.update(singletonList(new UpdateTestEntityWithAuditedFieldsCommand(ID_1)
                                                           .with(TestEntityWithAuditedFieldsType.DESC, "newDescA")),
                                         entityWithAuditedFieldsConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<TestEntityWithAuditedFieldsType> auditRecord = typed(auditRecords.get(0));
        assertThat(auditRecord, allOf(hasEntityType(TestEntityWithAuditedFieldsType.INSTANCE),
                                      hasEntityId(String.valueOf(ID_1)),
                                      hasOperator(UPDATE),
                                      not(hasFieldRecordFor(TestEntityWithAuditedFieldsType.NAME)),
                                      hasChangedFieldRecord(TestEntityWithAuditedFieldsType.DESC, "descA", "newDescA"),
                                      not(hasFieldRecordFor(TestEntityWithAuditedFieldsType.DESC2))));
    }

    @Test
    public void oneAuditedEntityWithoutDataFields_Exists_ShouldReturnEmpty() {
        auditedEntityWithoutDataFieldsPL.update(singletonList(new UpdateTestAuditedEntityWithoutDataFieldsCommand(ID_1)),
                                                auditedEntityWithoutDataFieldsConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat(auditRecords, is(empty()));
    }

    @Test
    public void oneNotAuditedEntity_FieldsChanged_ShouldReturnEmpty() {
        notAuditedEntityPL.update(singletonList(new UpdateTestEntityCommand(ID_1)
                                                    .with(TestEntityType.NAME, "newNameA")),
                                  notAuditedEntityConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("There should not be any published audit records",
                   auditRecords, is(empty()));
    }

    private <E extends EntityType<E>> ChangeFlowConfig<E> flowConfig(final E entityType) {
        return ChangeFlowConfigBuilderFactory.newInstance(plContext, entityType).build();
    }

    private <E extends EntityType<E>> PersistenceLayer<E> persistenceLayer() {
        return new PersistenceLayer<>(plContext);
    }

    @SuppressWarnings("unchecked")
    private <E extends EntityType<E>> AuditRecord<E> typed(final AuditRecord<?> auditRecord) {
        return (AuditRecord<E>) auditRecord;
    }
}
