package com.kenshoo.pl.audit;

import com.google.common.collect.ImmutableList;
import com.kenshoo.jooq.DataTableUtils;
import com.kenshoo.jooq.TestJooqConfig;
import com.kenshoo.pl.audit.commands.*;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.audit.AuditRecord;
import com.kenshoo.pl.entity.internal.audit.MainTable;
import com.kenshoo.pl.entity.internal.audit.entitytypes.*;
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

    private ChangeFlowConfig<AuditedType> auditedConfig;
    private ChangeFlowConfig<InclusiveAuditedType> inclusiveAuditedConfig;
    private ChangeFlowConfig<ExclusiveAuditedType> exclusiveAuditedConfig;
    private ChangeFlowConfig<AuditedWithoutDataFieldsType> auditedWithoutDataFieldsConfig;
    private ChangeFlowConfig<NotAuditedType> notAuditedConfig;

    private PersistenceLayer<AuditedType> auditedPL;
    private PersistenceLayer<InclusiveAuditedType> inclusiveAuditedPL;
    private PersistenceLayer<ExclusiveAuditedType> exclusiveAuditedPL;
    private PersistenceLayer<AuditedWithoutDataFieldsType> auditedWithoutDataFieldsPL;
    private PersistenceLayer<NotAuditedType> notAuditedPL;

    @Before
    public void setUp() {
        final DSLContext dslContext = TestJooqConfig.create();
        auditRecordPublisher = new InMemoryAuditRecordPublisher();
        plContext = new PLContext.Builder(dslContext)
            .withFeaturePredicate(__ -> true)
            .withAuditRecordPublisher(auditRecordPublisher)
            .build();

        auditedConfig = flowConfig(AuditedType.INSTANCE);
        inclusiveAuditedConfig = flowConfig(InclusiveAuditedType.INSTANCE);
        exclusiveAuditedConfig = flowConfig(ExclusiveAuditedType.INSTANCE);
        auditedWithoutDataFieldsConfig = flowConfig(AuditedWithoutDataFieldsType.INSTANCE);
        notAuditedConfig = flowConfig(NotAuditedType.INSTANCE);

        auditedPL = persistenceLayer();
        inclusiveAuditedPL = persistenceLayer();
        exclusiveAuditedPL = persistenceLayer();
        auditedWithoutDataFieldsPL = persistenceLayer();
        notAuditedPL = persistenceLayer();

        Stream.of(MainTable.INSTANCE)
              .forEach(table -> DataTableUtils.createTable(dslContext, table));

    }

    @After
    public void tearDown() {
        Stream.of(MainTable.INSTANCE)
              .forEach(table -> plContext.dslContext().dropTable(table).execute());
    }

    @Test
    public void oneAuditedEntity_AllFieldsInCommand_ShouldCreateFieldRecordsForAll() {
        final CreateResult<AuditedType, Identifier<AuditedType>> createResult =
            auditedPL.create(singletonList(new CreateAuditedCommand()
                                               .with(AuditedType.NAME, "name")
                                               .with(AuditedType.DESC, "desc")
                                               .with(AuditedType.DESC2, "desc2")),
                             auditedConfig);
        final long id = extractIdFromResult(createResult, AuditedType.ID);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<AuditedType> auditRecord = typed(auditRecords.get(0));
        assertThat(auditRecord, allOf(hasEntityType(AuditedType.INSTANCE.getName()),
                                      hasEntityId(String.valueOf(id)),
                                      hasOperator(CREATE),
                                      hasCreatedFieldRecord(AuditedType.NAME, "name"),
                                      hasCreatedFieldRecord(AuditedType.DESC, "desc"),
                                      hasCreatedFieldRecord(AuditedType.DESC2, "desc2")));
    }

    @Test
    public void oneAuditedEntity_SomeFieldsInCommand_ShouldCreateFieldRecordsForThemOnly() {
        auditedPL.create(singletonList(new CreateAuditedCommand()
                                           .with(AuditedType.NAME, "name")
                                           .with(AuditedType.DESC, "desc")),
                         auditedConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<AuditedType> auditRecord = typed(auditRecords.get(0));
        assertThat(auditRecord, allOf(hasCreatedFieldRecord(AuditedType.NAME, "name"),
                                      hasCreatedFieldRecord(AuditedType.DESC, "desc"),
                                      not(hasFieldRecordFor(AuditedType.DESC2))));
    }

    @Test
    public void twoAuditedEntities_AllFieldsInCommands_ShouldCreateFieldRecordsForAll() {
        final List<CreateAuditedCommand> cmds =
            ImmutableList.of(new CreateAuditedCommand()
                                 .with(AuditedType.NAME, "nameA")
                                 .with(AuditedType.DESC, "descA")
                                 .with(AuditedType.DESC2, "desc2A"),
                             new CreateAuditedCommand()
                                 .with(AuditedType.NAME, "nameB")
                                 .with(AuditedType.DESC, "descB")
                                 .with(AuditedType.DESC2, "desc2B"));

        final CreateResult<AuditedType, Identifier<AuditedType>> createResult =
            auditedPL.create(cmds, auditedConfig);
        final List<Long> ids = extractIdsFromResult(createResult,
                                                    AuditedType.ID,
                                                    AuditedType.NAME);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of generated ids",
                   ids, hasSize(2));
        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(2));

        final AuditRecord<AuditedType> auditRecord1 = typed(auditRecords.get(0));
        assertThat(auditRecord1, allOf(hasEntityType(AuditedType.INSTANCE.getName()),
                                       hasEntityId(String.valueOf(ids.get(0))),
                                       hasOperator(CREATE),
                                       hasCreatedFieldRecord(AuditedType.NAME, "nameA"),
                                       hasCreatedFieldRecord(AuditedType.DESC, "descA"),
                                       hasCreatedFieldRecord(AuditedType.DESC2, "desc2A")));

        final AuditRecord<AuditedType> auditRecord2 = typed(auditRecords.get(1));
        assertThat(auditRecord2, allOf(hasEntityType(AuditedType.INSTANCE.getName()),
                                       hasEntityId(String.valueOf(ids.get(1))),
                                       hasOperator(CREATE),
                                       hasCreatedFieldRecord(AuditedType.NAME, "nameB"),
                                       hasCreatedFieldRecord(AuditedType.DESC, "descB"),
                                       hasCreatedFieldRecord(AuditedType.DESC2, "desc2B")));
    }

    @Test
    public void oneInclusiveAuditedEntity_AllEntityFieldsInCommand_ShouldCreateFieldRecordsForAuditedOnly() {
        inclusiveAuditedPL.create(singletonList(new CreateInclusiveAuditedCommand()
                                                    .with(InclusiveAuditedType.NAME, "name")
                                                    .with(InclusiveAuditedType.DESC, "desc")
                                                    .with(InclusiveAuditedType.DESC2, "desc2")),
                                  inclusiveAuditedConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<InclusiveAuditedType> auditRecord = typed(auditRecords.get(0));
        assertThat(auditRecord, allOf(hasCreatedFieldRecord(InclusiveAuditedType.NAME, "name"),
                                      hasCreatedFieldRecord(InclusiveAuditedType.DESC, "desc"),
                                      not(hasFieldRecordFor(InclusiveAuditedType.DESC2))));
    }

    @Test
    public void oneInclusiveAuditedEntity_SameFieldsInCommand_ShouldCreateFieldRecordsForThem() {
        inclusiveAuditedPL.create(singletonList(new CreateInclusiveAuditedCommand()
                                                    .with(InclusiveAuditedType.NAME, "name")
                                                    .with(InclusiveAuditedType.DESC, "desc")),
                                  inclusiveAuditedConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<InclusiveAuditedType> auditRecord = typed(auditRecords.get(0));
        assertThat(auditRecord, allOf(hasCreatedFieldRecord(InclusiveAuditedType.NAME, "name"),
                                      hasCreatedFieldRecord(InclusiveAuditedType.DESC, "desc"),
                                      not(hasFieldRecordFor(InclusiveAuditedType.DESC2))));
    }

    @Test
    public void oneInclusiveAuditedEntity_PartiallyIntersectCommand_ShouldCreateFieldRecordsForIntersectionOnly() {
        inclusiveAuditedPL.create(singletonList(new CreateInclusiveAuditedCommand()
                                                    .with(InclusiveAuditedType.DESC, "desc")
                                                    .with(InclusiveAuditedType.DESC2, "desc2")),
                                  inclusiveAuditedConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<InclusiveAuditedType> auditRecord = typed(auditRecords.get(0));
        assertThat(auditRecord, allOf(hasCreatedFieldRecord(InclusiveAuditedType.DESC, "desc"),
                                      not(hasFieldRecordFor(InclusiveAuditedType.NAME)),
                                      not(hasFieldRecordFor(InclusiveAuditedType.DESC2))));
    }

    @Test
    public void oneInclusiveAuditedEntity_DoesntIntersectCommand_ShouldReturnFixedDataOnly() {
        final CreateResult<InclusiveAuditedType, Identifier<InclusiveAuditedType>> createResult =
            inclusiveAuditedPL.create(singletonList(new CreateInclusiveAuditedCommand()
                                                        .with(InclusiveAuditedType.DESC2, "desc2")),
                                      inclusiveAuditedConfig);
        final long id = extractIdFromResult(createResult, InclusiveAuditedType.ID);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<InclusiveAuditedType> auditRecord = typed(auditRecords.get(0));
        assertThat(auditRecord, allOf(hasEntityType(InclusiveAuditedType.INSTANCE.getName()),
                                      hasEntityId(String.valueOf(id)),
                                      hasOperator(CREATE),
                                      hasNoFieldRecords()));
    }

    @Test
    public void oneExclusiveAuditedEntity_AllEntityFieldsInCommand_ShouldCreateFieldRecordsForAuditedOnly() {
        exclusiveAuditedPL.create(singletonList(new CreateExclusiveAuditedCommand()
                                                               .with(ExclusiveAuditedType.NAME, "name")
                                                               .with(ExclusiveAuditedType.DESC, "desc")
                                                               .with(ExclusiveAuditedType.DESC2, "desc2")),
                                  exclusiveAuditedConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<ExclusiveAuditedType> auditRecord = typed(auditRecords.get(0));
        assertThat(auditRecord, allOf(hasCreatedFieldRecord(ExclusiveAuditedType.NAME, "name"),
                                      not(hasFieldRecordFor(ExclusiveAuditedType.DESC)),
                                      not(hasFieldRecordFor(ExclusiveAuditedType.DESC2))));
    }

    @Test
    public void oneAuditedEntityWithoutDataFields_ShouldCreateRecordWithFixedDataOnly() {
        auditedWithoutDataFieldsPL.create(singletonList(new CreateAuditedWithoutDataFieldsCommand(ID)),
                                          auditedWithoutDataFieldsConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<AuditedWithoutDataFieldsType> auditRecord = typed(auditRecords.get(0));
        assertThat(auditRecord, allOf(hasEntityType(AuditedWithoutDataFieldsType.INSTANCE.getName()),
                                      hasEntityId(String.valueOf(ID)),
                                      hasOperator(CREATE)));
    }

    @Test
    public void oneNotAuditedEntity_WithFieldsInCommand_ShouldReturnEmpty() {
        notAuditedPL.create(singletonList(new CreateNotAuditedCommand()
                                              .with(NotAuditedType.NAME, "name")),
                            notAuditedConfig);

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
