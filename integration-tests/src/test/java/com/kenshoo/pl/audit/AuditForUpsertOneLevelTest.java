package com.kenshoo.pl.audit;

import com.google.common.collect.ImmutableList;
import com.kenshoo.jooq.DataTableUtils;
import com.kenshoo.jooq.TestJooqConfig;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.audit.TestAuditedEntityType;
import com.kenshoo.pl.entity.internal.audit.TestEntityTable;
import com.kenshoo.pl.entity.internal.audit.TestEntityType;
import org.jooq.DSLContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.stream.Stream;

import static com.kenshoo.pl.entity.ChangeOperation.CREATE;
import static com.kenshoo.pl.entity.ChangeOperation.UPDATE;
import static com.kenshoo.pl.entity.matchers.audit.AuditRecordMatchers.*;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class AuditForUpsertOneLevelTest {

    private static final long EXISTING_ID_1 = 1L;
    private static final long EXISTING_ID_2 = 2L;

    private static final String EXISTING_NAME_1 = "nameA";
    private static final String EXISTING_NAME_2 = "nameB";

    private static final String NEW_NAME_1 = "newNameA";
    private static final String NEW_NAME_2 = "newNameB";

    private PLContext plContext;
    private DSLContext dslContext;
    private InMemoryAuditRecordPublisher auditRecordPublisher;

    private ChangeFlowConfig<TestAuditedEntityType> auditedEntityConfig;
    private ChangeFlowConfig<TestEntityType> notAuditedEntityConfig;

    private PersistenceLayer<TestAuditedEntityType> auditedEntityPL;
    private PersistenceLayer<TestEntityType> notAuditedEntityPL;

    @Before
    public void setUp() {
        dslContext = TestJooqConfig.create();
        auditRecordPublisher = new InMemoryAuditRecordPublisher();
        plContext = new PLContext.Builder(dslContext)
            .withFeaturePredicate(__ -> true)
            .withAuditRecordPublisher(auditRecordPublisher)
            .build();

        auditedEntityConfig = flowConfig(TestAuditedEntityType.INSTANCE);
        notAuditedEntityConfig = flowConfig(TestEntityType.INSTANCE);

        auditedEntityPL = persistenceLayer();
        notAuditedEntityPL = persistenceLayer();

        Stream.of(TestEntityTable.INSTANCE)
              .forEach(table -> DataTableUtils.createTable(dslContext, table));

        dslContext.insertInto(TestEntityTable.INSTANCE)
                  .set(TestEntityTable.INSTANCE.id, EXISTING_ID_1)
                  .set(TestEntityTable.INSTANCE.name, EXISTING_NAME_1)
                  .set(TestEntityTable.INSTANCE.desc, "descA")
                  .set(TestEntityTable.INSTANCE.desc2, "desc2A")
                  .execute();
        dslContext.insertInto(TestEntityTable.INSTANCE)
                  .set(TestEntityTable.INSTANCE.id, EXISTING_ID_2)
                  .set(TestEntityTable.INSTANCE.name, EXISTING_NAME_2)
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
    public void oneAuditedEntity_New_ShouldReturnCreateOperator_AndCreatedFieldRecords() {
        auditedEntityPL.upsert(singletonList(new UpsertTestAuditedEntityCommand(NEW_NAME_1)
                                                 .with(TestAuditedEntityType.DESC, "desc")
                                                 .with(TestAuditedEntityType.DESC2, "desc2")),
                               auditedEntityConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<TestAuditedEntityType> auditRecord = typed(auditRecords.get(0));
        assertThat(auditRecord, allOf(hasEntityType(TestAuditedEntityType.INSTANCE),
                                      hasEntityId(String.valueOf(fetchIdByName(NEW_NAME_1))),
                                      hasOperator(CREATE),
                                      hasCreatedFieldRecord(TestAuditedEntityType.NAME, NEW_NAME_1),
                                      hasCreatedFieldRecord(TestAuditedEntityType.DESC, "desc"),
                                      hasCreatedFieldRecord(TestAuditedEntityType.DESC2, "desc2")));
    }

    @Test
    public void oneAuditedEntity_Existing_ShouldReturnUpdateOperator_AndChangedFieldRecordsExceptKey() {
        auditedEntityPL.upsert(singletonList(new UpsertTestAuditedEntityCommand(EXISTING_NAME_1)
                                                 .with(TestAuditedEntityType.DESC, "newDescA")
                                                 .with(TestAuditedEntityType.DESC2, "newDesc2A")),
                               auditedEntityConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<TestAuditedEntityType> auditRecord = typed(auditRecords.get(0));
        assertThat(auditRecord, allOf(hasEntityType(TestAuditedEntityType.INSTANCE),
                                      hasEntityId(String.valueOf(EXISTING_ID_1)),
                                      hasOperator(UPDATE),
                                      not(hasFieldRecordFor(TestAuditedEntityType.NAME)),
                                      hasChangedFieldRecord(TestAuditedEntityType.DESC, "descA", "newDescA"),
                                      hasChangedFieldRecord(TestAuditedEntityType.DESC2, "desc2A", "newDesc2A")));
    }

    @Test
    public void oneAuditedEntity_Existing_NoFieldsChanged_ShouldReturnEmpty() {
        auditedEntityPL.upsert(singletonList(new UpsertTestAuditedEntityCommand(EXISTING_NAME_1)
                                                 .with(TestAuditedEntityType.DESC, "descA")
                                                 .with(TestAuditedEntityType.DESC2, "desc2A")),
                               auditedEntityConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat(auditRecords, is(empty()));
    }

    @Test
    public void twoAuditedEntities_BothNew_ShouldReturnCreateOperator_AndCreatedFieldRecords() {
        final List<UpsertTestAuditedEntityCommand> cmds =
            ImmutableList.of(new UpsertTestAuditedEntityCommand(NEW_NAME_1)
                                 .with(TestAuditedEntityType.DESC, "newDescA")
                                 .with(TestAuditedEntityType.DESC2, "newDesc2A"),
                             new UpsertTestAuditedEntityCommand(NEW_NAME_2)
                                 .with(TestAuditedEntityType.DESC, "newDescB")
                                 .with(TestAuditedEntityType.DESC2, "newDesc2B"));

        auditedEntityPL.upsert(cmds, auditedEntityConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(2));

        final AuditRecord<TestAuditedEntityType> auditRecord1 = typed(auditRecords.get(0));
        assertThat(auditRecord1, allOf(hasEntityType(TestAuditedEntityType.INSTANCE),
                                       hasEntityId(String.valueOf(fetchIdByName(NEW_NAME_1))),
                                       hasOperator(CREATE),
                                       hasCreatedFieldRecord(TestAuditedEntityType.NAME, NEW_NAME_1),
                                       hasCreatedFieldRecord(TestAuditedEntityType.DESC, "newDescA"),
                                       hasCreatedFieldRecord(TestAuditedEntityType.DESC2, "newDesc2A")));

        final AuditRecord<TestAuditedEntityType> auditRecord2 = typed(auditRecords.get(1));
        assertThat(auditRecord2, allOf(hasEntityType(TestAuditedEntityType.INSTANCE),
                                       hasEntityId(String.valueOf(fetchIdByName(NEW_NAME_2))),
                                       hasOperator(CREATE),
                                       hasCreatedFieldRecord(TestAuditedEntityType.NAME, NEW_NAME_2),
                                       hasCreatedFieldRecord(TestAuditedEntityType.DESC, "newDescB"),
                                       hasCreatedFieldRecord(TestAuditedEntityType.DESC2, "newDesc2B")));
    }

    @Test
    public void twoAuditedEntities_BothExisting_ShouldReturnUpdateOperator_AndChangedFieldRecordsExceptKey() {
        final List<UpsertTestAuditedEntityCommand> cmds =
            ImmutableList.of(new UpsertTestAuditedEntityCommand(EXISTING_NAME_1)
                                 .with(TestAuditedEntityType.DESC, "newDescA")
                                 .with(TestAuditedEntityType.DESC2, "newDesc2A"),
                             new UpsertTestAuditedEntityCommand(EXISTING_NAME_2)
                                 .with(TestAuditedEntityType.DESC, "newDescB")
                                 .with(TestAuditedEntityType.DESC2, "newDesc2B"));

        auditedEntityPL.upsert(cmds, auditedEntityConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(2));

        final AuditRecord<TestAuditedEntityType> auditRecord1 = typed(auditRecords.get(0));
        assertThat(auditRecord1, allOf(hasEntityType(TestAuditedEntityType.INSTANCE),
                                       hasEntityId(String.valueOf(EXISTING_ID_1)),
                                       hasOperator(UPDATE),
                                       not(hasFieldRecordFor(TestAuditedEntityType.NAME)),
                                       hasChangedFieldRecord(TestAuditedEntityType.DESC, "descA", "newDescA"),
                                       hasChangedFieldRecord(TestAuditedEntityType.DESC2, "desc2A", "newDesc2A")));

        final AuditRecord<TestAuditedEntityType> auditRecord2 = typed(auditRecords.get(1));
        assertThat(auditRecord2, allOf(hasEntityType(TestAuditedEntityType.INSTANCE),
                                       hasEntityId(String.valueOf(EXISTING_ID_2)),
                                       hasOperator(UPDATE),
                                       not(hasFieldRecordFor(TestAuditedEntityType.NAME)),
                                       hasChangedFieldRecord(TestAuditedEntityType.DESC, "descB", "newDescB"),
                                       hasChangedFieldRecord(TestAuditedEntityType.DESC2, "desc2B", "newDesc2B")));
    }

    @Test
    public void twoAuditedEntities_OneNew_OneExistingWithChanges_ShouldReturnProperOperators_AndFieldRecords() {
        final List<UpsertTestAuditedEntityCommand> cmds =
            ImmutableList.of(new UpsertTestAuditedEntityCommand(NEW_NAME_1)
                                 .with(TestAuditedEntityType.DESC, "newDescA")
                                 .with(TestAuditedEntityType.DESC2, "newDesc2A"),
                             new UpsertTestAuditedEntityCommand(EXISTING_NAME_2)
                                 .with(TestAuditedEntityType.DESC, "newDescB")
                                 .with(TestAuditedEntityType.DESC2, "newDesc2B"));

        auditedEntityPL.upsert(cmds, auditedEntityConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(2));

        final AuditRecord<TestAuditedEntityType> auditRecord1 = typed(auditRecords.get(0));
        assertThat(auditRecord1, allOf(hasEntityType(TestAuditedEntityType.INSTANCE),
                                       hasEntityId(String.valueOf(fetchIdByName(NEW_NAME_1))),
                                       hasOperator(CREATE),
                                       hasCreatedFieldRecord(TestAuditedEntityType.NAME, NEW_NAME_1),
                                       hasCreatedFieldRecord(TestAuditedEntityType.DESC, "newDescA"),
                                       hasCreatedFieldRecord(TestAuditedEntityType.DESC2, "newDesc2A")));

        final AuditRecord<TestAuditedEntityType> auditRecord2 = typed(auditRecords.get(1));
        assertThat(auditRecord2, allOf(hasEntityType(TestAuditedEntityType.INSTANCE),
                                       hasEntityId(String.valueOf(EXISTING_ID_2)),
                                       hasOperator(UPDATE),
                                       not(hasFieldRecordFor(TestAuditedEntityType.NAME)),
                                       hasChangedFieldRecord(TestAuditedEntityType.DESC, "descB", "newDescB"),
                                       hasChangedFieldRecord(TestAuditedEntityType.DESC2, "desc2B", "newDesc2B")));
    }

    @Test
    public void oneNotAuditedEntity_New_ShouldReturnEmpty() {
        notAuditedEntityPL.upsert(singletonList(new UpsertTestEntityCommand("name")),
                                  notAuditedEntityConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat(auditRecords, is(empty()));
    }

    @Test
    public void oneNotAuditedEntity_Existing_FieldsChanged_ShouldReturnEmpty() {
        notAuditedEntityPL.upsert(singletonList(new UpsertTestEntityCommand(EXISTING_NAME_1)
                                                    .with(TestEntityType.DESC, "newDescA")),
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

    private long fetchIdByName(final String name) {
        return dslContext.select(TestEntityTable.INSTANCE.id)
                         .from(TestEntityTable.INSTANCE)
                         .where(TestEntityTable.INSTANCE.name.eq(name))
                         .fetchOptional(TestEntityTable.INSTANCE.id)
                         .orElseThrow(() -> new IllegalStateException("Could not fetch the id of the entity named '" + name + "'"));
    }

    @SuppressWarnings("unchecked")
    private <E extends EntityType<E>> AuditRecord<E> typed(final AuditRecord<?> auditRecord) {
        return (AuditRecord<E>) auditRecord;
    }
}
