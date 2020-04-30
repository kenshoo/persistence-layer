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
import java.util.Objects;
import java.util.stream.Stream;

import static com.kenshoo.pl.entity.ChangeOperation.CREATE;
import static com.kenshoo.pl.entity.matchers.audit.AuditRecordMatchers.*;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class AuditForCreateOneLevelTest {

    private static final long ID = 1L;

    private PLContext plContext;
    private InMemoryAuditRecordPublisher auditRecordPublisher;

    private ChangeFlowConfig<TestAuditedEntityType> auditedEntityConfig;
    private ChangeFlowConfig<TestEntityWithAuditedFieldsType> entityWithAuditedFieldsConfig;
    private ChangeFlowConfig<TestAuditedEntityWithNotAuditedFieldsType> auditedEntityWithNotAuditedFieldsConfig;
    private ChangeFlowConfig<TestAuditedEntityWithoutDataFieldsType> auditedEntityWithoutDataFieldsConfig;
    private ChangeFlowConfig<TestEntityType> notAuditedEntityConfig;

    private PersistenceLayer<TestAuditedEntityType> auditedEntityPL;
    private PersistenceLayer<TestEntityWithAuditedFieldsType> entityWithAuditedFieldsPL;
    private PersistenceLayer<TestAuditedEntityWithNotAuditedFieldsType> auditedEntityWithNotAuditedFieldsPL;
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
        auditedEntityWithNotAuditedFieldsConfig = flowConfig(TestAuditedEntityWithNotAuditedFieldsType.INSTANCE);
        auditedEntityWithoutDataFieldsConfig = flowConfig(TestAuditedEntityWithoutDataFieldsType.INSTANCE);
        notAuditedEntityConfig = flowConfig(TestEntityType.INSTANCE);

        auditedEntityPL = persistenceLayer();
        entityWithAuditedFieldsPL = persistenceLayer();
        auditedEntityWithNotAuditedFieldsPL = persistenceLayer();
        auditedEntityWithoutDataFieldsPL = persistenceLayer();
        notAuditedEntityPL = persistenceLayer();

        Stream.of(TestEntityTable.INSTANCE)
              .forEach(table -> DataTableUtils.createTable(dslContext, table));

    }

    @After
    public void tearDown() {
        Stream.of(TestEntityTable.INSTANCE)
              .forEach(table -> plContext.dslContext().dropTable(table).execute());
    }

    @Test
    public void oneAuditedEntity_AllFieldsInCommand_ShouldCreateFieldRecordsForAll() {
        final CreateResult<TestAuditedEntityType, Identifier<TestAuditedEntityType>> createResult =
            auditedEntityPL.create(singletonList(new CreateTestAuditedEntityCommand()
                                                     .with(TestAuditedEntityType.NAME, "name")
                                                     .with(TestAuditedEntityType.DESC, "desc")
                                                     .with(TestAuditedEntityType.DESC2, "desc2")),
                                   auditedEntityConfig);
        final long id = extractIdFromResult(createResult, TestAuditedEntityType.ID);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<TestAuditedEntityType> auditRecord = typed(auditRecords.get(0));
        assertThat(auditRecord, allOf(hasEntityType(TestAuditedEntityType.INSTANCE),
                                      hasEntityId(String.valueOf(id)),
                                      hasOperator(CREATE),
                                      hasCreatedFieldRecord(TestAuditedEntityType.NAME, "name"),
                                      hasCreatedFieldRecord(TestAuditedEntityType.DESC, "desc"),
                                      hasCreatedFieldRecord(TestAuditedEntityType.DESC2, "desc2")));
    }

    @Test
    public void oneAuditedEntity_SomeFieldsInCommand_ShouldCreateFieldRecordsForThemOnly() {
        auditedEntityPL.create(singletonList(new CreateTestAuditedEntityCommand()
                                                 .with(TestAuditedEntityType.NAME, "name")
                                                 .with(TestAuditedEntityType.DESC, "desc")),
                               auditedEntityConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<TestAuditedEntityType> auditRecord = typed(auditRecords.get(0));
        assertThat(auditRecord, allOf(hasCreatedFieldRecord(TestAuditedEntityType.NAME, "name"),
                                      hasCreatedFieldRecord(TestAuditedEntityType.DESC, "desc"),
                                      not(hasFieldRecordFor(TestAuditedEntityType.DESC2))));
    }

    @Test
    public void twoAuditedEntities_AllFieldsInCommands_ShouldCreateFieldRecordsForAll() {
        final List<CreateTestAuditedEntityCommand> cmds =
            ImmutableList.of(new CreateTestAuditedEntityCommand()
                                 .with(TestAuditedEntityType.NAME, "nameA")
                                 .with(TestAuditedEntityType.DESC, "descA")
                                 .with(TestAuditedEntityType.DESC2, "desc2A"),
                             new CreateTestAuditedEntityCommand()
                                 .with(TestAuditedEntityType.NAME, "nameB")
                                 .with(TestAuditedEntityType.DESC, "descB")
                                 .with(TestAuditedEntityType.DESC2, "desc2B"));

        final CreateResult<TestAuditedEntityType, Identifier<TestAuditedEntityType>> createResult =
            auditedEntityPL.create(cmds, auditedEntityConfig);
        final List<Long> ids = extractIdsFromResult(createResult,
                                                    TestAuditedEntityType.ID,
                                                    TestAuditedEntityType.NAME);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of generated ids",
                   ids, hasSize(2));
        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(2));

        final AuditRecord<TestAuditedEntityType> auditRecord1 = typed(auditRecords.get(0));
        assertThat(auditRecord1, allOf(hasEntityType(TestAuditedEntityType.INSTANCE),
                                       hasEntityId(String.valueOf(ids.get(0))),
                                       hasOperator(CREATE),
                                       hasCreatedFieldRecord(TestAuditedEntityType.NAME, "nameA"),
                                       hasCreatedFieldRecord(TestAuditedEntityType.DESC, "descA"),
                                       hasCreatedFieldRecord(TestAuditedEntityType.DESC2, "desc2A")));

        final AuditRecord<TestAuditedEntityType> auditRecord2 = typed(auditRecords.get(1));
        assertThat(auditRecord2, allOf(hasEntityType(TestAuditedEntityType.INSTANCE),
                                       hasEntityId(String.valueOf(ids.get(1))),
                                       hasOperator(CREATE),
                                       hasCreatedFieldRecord(TestAuditedEntityType.NAME, "nameB"),
                                       hasCreatedFieldRecord(TestAuditedEntityType.DESC, "descB"),
                                       hasCreatedFieldRecord(TestAuditedEntityType.DESC2, "desc2B")));
    }

    @Test
    public void oneEntityWithAuditedFields_AllEntityFieldsInCommand_ShouldCreateFieldRecordsForAuditedOnly() {
        entityWithAuditedFieldsPL.create(singletonList(new CreateTestEntityWithAuditedFieldsCommand()
                                                           .with(TestEntityWithAuditedFieldsType.NAME, "name")
                                                           .with(TestEntityWithAuditedFieldsType.DESC, "desc")
                                                           .with(TestEntityWithAuditedFieldsType.DESC2, "desc2")),
                                         entityWithAuditedFieldsConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<TestEntityWithAuditedFieldsType> auditRecord = typed(auditRecords.get(0));
        assertThat(auditRecord, allOf(hasCreatedFieldRecord(TestEntityWithAuditedFieldsType.NAME, "name"),
                                      hasCreatedFieldRecord(TestEntityWithAuditedFieldsType.DESC, "desc"),
                                      not(hasFieldRecordFor(TestEntityWithAuditedFieldsType.DESC2))));
    }

    @Test
    public void oneEntityWithAuditedFields_SameFieldsInCommand_ShouldCreateFieldRecordsForThem() {
        entityWithAuditedFieldsPL.create(singletonList(new CreateTestEntityWithAuditedFieldsCommand()
                                                           .with(TestEntityWithAuditedFieldsType.NAME, "name")
                                                           .with(TestEntityWithAuditedFieldsType.DESC, "desc")),
                                         entityWithAuditedFieldsConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<TestEntityWithAuditedFieldsType> auditRecord = typed(auditRecords.get(0));
        assertThat(auditRecord, allOf(hasCreatedFieldRecord(TestEntityWithAuditedFieldsType.NAME, "name"),
                                      hasCreatedFieldRecord(TestEntityWithAuditedFieldsType.DESC, "desc"),
                                      not(hasFieldRecordFor(TestEntityWithAuditedFieldsType.DESC2))));
    }

    @Test
    public void oneEntityWithAuditedFields_PartiallyIntersectCommand_ShouldCreateFieldRecordsForIntersectionOnly() {
        entityWithAuditedFieldsPL.create(singletonList(new CreateTestEntityWithAuditedFieldsCommand()
                                                           .with(TestEntityWithAuditedFieldsType.DESC, "desc")
                                                           .with(TestEntityWithAuditedFieldsType.DESC2, "desc2")),
                                         entityWithAuditedFieldsConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<TestEntityWithAuditedFieldsType> auditRecord = typed(auditRecords.get(0));
        assertThat(auditRecord, allOf(hasCreatedFieldRecord(TestEntityWithAuditedFieldsType.DESC, "desc"),
                                      not(hasFieldRecordFor(TestEntityWithAuditedFieldsType.NAME)),
                                      not(hasFieldRecordFor(TestEntityWithAuditedFieldsType.DESC2))));
    }

    @Test
    public void oneEntityWithAuditedFields_DoesntIntersectCommand_ShouldReturnFixedDataOnly() {
        final CreateResult<TestEntityWithAuditedFieldsType, Identifier<TestEntityWithAuditedFieldsType>> createResult =
            entityWithAuditedFieldsPL.create(singletonList(new CreateTestEntityWithAuditedFieldsCommand()
                                                               .with(TestEntityWithAuditedFieldsType.DESC2, "desc2")),
                                             entityWithAuditedFieldsConfig);
        final long id = extractIdFromResult(createResult, TestEntityWithAuditedFieldsType.ID);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<TestEntityWithAuditedFieldsType> auditRecord = typed(auditRecords.get(0));
        assertThat(auditRecord, allOf(hasEntityType(TestEntityWithAuditedFieldsType.INSTANCE),
                                      hasEntityId(String.valueOf(id)),
                                      hasOperator(CREATE),
                                      hasNoFieldRecords()));
    }

    @Test
    public void oneAuditedEntityWithNotAuditedFields_AllEntityFieldsInCommand_ShouldCreateFieldRecordsForAuditedOnly() {
        auditedEntityWithNotAuditedFieldsPL.create(singletonList(new CreateTestAuditedEntityWithNotAuditedFieldsCommand()
                                                                     .with(TestAuditedEntityWithNotAuditedFieldsType.NAME, "name")
                                                                     .with(TestAuditedEntityWithNotAuditedFieldsType.DESC, "desc")
                                                                     .with(TestAuditedEntityWithNotAuditedFieldsType.DESC2, "desc2")),
                                                   auditedEntityWithNotAuditedFieldsConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<TestAuditedEntityWithNotAuditedFieldsType> auditRecord = typed(auditRecords.get(0));
        assertThat(auditRecord, allOf(hasCreatedFieldRecord(TestAuditedEntityWithNotAuditedFieldsType.NAME, "name"),
                                      not(hasFieldRecordFor(TestAuditedEntityWithNotAuditedFieldsType.DESC)),
                                      not(hasFieldRecordFor(TestAuditedEntityWithNotAuditedFieldsType.DESC2))));
    }

    @Test
    public void oneAuditedEntityWithoutDataFields_ShouldCreateRecordWithFixedDataOnly() {
        auditedEntityWithoutDataFieldsPL.create(singletonList(new CreateTestAuditedEntityWithoutDataFieldsCommand(ID)),
                                                auditedEntityWithoutDataFieldsConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<TestAuditedEntityWithoutDataFieldsType> auditRecord = typed(auditRecords.get(0));
        assertThat(auditRecord, allOf(hasEntityType(TestAuditedEntityWithoutDataFieldsType.INSTANCE),
                                      hasEntityId(String.valueOf(ID)),
                                      hasOperator(CREATE)));
    }

    @Test
    public void oneNotAuditedEntity_WithFieldsInCommand_ShouldReturnEmpty() {
        notAuditedEntityPL.create(singletonList(new CreateTestEntityCommand()
                                                    .with(TestEntityType.NAME, "name")),
                                  notAuditedEntityConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat(auditRecords, is(empty()));
    }

    private <E extends EntityType<E>> ChangeFlowConfig<E> flowConfig(final E entityType) {
        return ChangeFlowConfigBuilderFactory.newInstance(plContext, entityType).build();
    }

    private <E extends EntityType<E>> PersistenceLayer<E> persistenceLayer() {
        return new PersistenceLayer<>(plContext);
    }

    private <E extends EntityType<E>> long extractIdFromResult(final CreateResult<E, Identifier<E>> createResult,
                                                               final EntityField<E, Long> idField) {
        return createResult.getChangeResults().stream()
                           .map(EntityChangeResult::getCommand)
                           .map(CreateEntityCommand::getIdentifier)
                           .filter(Objects::nonNull)
                           .map(identifier -> identifier.get(idField))
                           .findFirst()
                           .orElseThrow(() -> new IllegalStateException("No ids returned by create operation"));
    }

    private <E extends EntityType<E>> List<Long> extractIdsFromResult(final CreateResult<E, Identifier<E>> createResult,
                                                                      final EntityField<E, Long> idField,
                                                                      final EntityField<E, String> sortField) {
        return createResult.getChangeResults().stream()
                           .map(EntityChangeResult::getCommand)
                           .sorted(comparing(cmd -> cmd.get(sortField)))
                           .map(CreateEntityCommand::getIdentifier)
                           .filter(Objects::nonNull)
                           .map(identifier -> identifier.get(idField))
                           .filter(Objects::nonNull)
                           .collect(toList());
    }

    @SuppressWarnings("unchecked")
    private <E extends EntityType<E>> AuditRecord<E> typed(final AuditRecord<?> auditRecord) {
        return (AuditRecord<E>) auditRecord;
    }
}
