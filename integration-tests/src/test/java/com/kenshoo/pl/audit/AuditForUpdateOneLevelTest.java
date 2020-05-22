package com.kenshoo.pl.audit;

import com.google.common.collect.ImmutableList;
import com.kenshoo.jooq.DataTableUtils;
import com.kenshoo.jooq.TestJooqConfig;
import com.kenshoo.pl.audit.commands.UpdateAuditedCommand;
import com.kenshoo.pl.audit.commands.UpdateAuditedWithoutDataFieldsCommand;
import com.kenshoo.pl.audit.commands.UpdateInclusiveAuditedCommand;
import com.kenshoo.pl.audit.commands.UpdateNotAuditedCommand;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.audit.AuditRecord;
import com.kenshoo.pl.entity.internal.audit.MainTable;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithoutDataFieldsType;
import com.kenshoo.pl.entity.internal.audit.entitytypes.InclusiveAuditedType;
import com.kenshoo.pl.entity.internal.audit.entitytypes.NotAuditedType;
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

    private ChangeFlowConfig<AuditedType> auditedConfig;
    private ChangeFlowConfig<InclusiveAuditedType> inclusiveAuditedConfig;
    private ChangeFlowConfig<AuditedWithoutDataFieldsType> auditedWithoutDataFieldsConfig;
    private ChangeFlowConfig<NotAuditedType> notAuditedConfig;

    private PersistenceLayer<AuditedType> auditedPL;
    private PersistenceLayer<InclusiveAuditedType> inclusiveAuditedPL;
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
        auditedWithoutDataFieldsConfig = flowConfig(AuditedWithoutDataFieldsType.INSTANCE);
        notAuditedConfig = flowConfig(NotAuditedType.INSTANCE);

        auditedPL = persistenceLayer();
        inclusiveAuditedPL = persistenceLayer();
        auditedWithoutDataFieldsPL = persistenceLayer();
        notAuditedPL = persistenceLayer();

        Stream.of(MainTable.INSTANCE)
              .forEach(table -> DataTableUtils.createTable(dslContext, table));

        dslContext.insertInto(MainTable.INSTANCE)
                  .set(MainTable.INSTANCE.id, ID_1)
                  .set(MainTable.INSTANCE.name, "nameA")
                  .set(MainTable.INSTANCE.desc, "descA")
                  .set(MainTable.INSTANCE.desc2, "desc2A")
                  .execute();
        dslContext.insertInto(MainTable.INSTANCE)
                  .set(MainTable.INSTANCE.id, ID_2)
                  .set(MainTable.INSTANCE.name, "nameB")
                  .set(MainTable.INSTANCE.desc, "descB")
                  .set(MainTable.INSTANCE.desc2, "desc2B")
                  .execute();
    }

    @After
    public void tearDown() {
        Stream.of(MainTable.INSTANCE)
              .forEach(table -> plContext.dslContext().dropTable(table).execute());
    }

    @Test
    public void oneAuditedEntity_AllFieldsChanged_ShouldCreateFieldRecordsForAll() {
        auditedPL.update(singletonList(new UpdateAuditedCommand(ID_1)
                                                 .with(AuditedType.NAME, "newNameA")
                                                 .with(AuditedType.DESC, "newDescA")
                                                 .with(AuditedType.DESC2, "newDesc2A")),
                         auditedConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<AuditedType> auditRecord = typed(auditRecords.get(0));
        assertThat(auditRecord, allOf(hasEntityType(AuditedType.INSTANCE),
                                      hasEntityId(String.valueOf(ID_1)),
                                      hasOperator(UPDATE),
                                      hasChangedFieldRecord(AuditedType.NAME, "nameA", "newNameA"),
                                      hasChangedFieldRecord(AuditedType.DESC, "descA", "newDescA"),
                                      hasChangedFieldRecord(AuditedType.DESC2, "desc2A", "newDesc2A")));
    }

    @Test
    public void oneAuditedEntity_AllFieldsInCmd_SomeFieldsChanged_ShouldCreateFieldRecordsForChangedOnly() {
        auditedPL.update(singletonList(new UpdateAuditedCommand(ID_1)
                                                 .with(AuditedType.NAME, "newNameA")
                                                 .with(AuditedType.DESC, "newDescA")
                                                 .with(AuditedType.DESC2, "desc2A")),
                         auditedConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<AuditedType> auditRecord = typed(auditRecords.get(0));
        assertThat(auditRecord, allOf(hasEntityType(AuditedType.INSTANCE),
                                      hasEntityId(String.valueOf(ID_1)),
                                      hasOperator(UPDATE),
                                      hasChangedFieldRecord(AuditedType.NAME, "nameA", "newNameA"),
                                      hasChangedFieldRecord(AuditedType.DESC, "descA", "newDescA"),
                                      not(hasFieldRecordFor(AuditedType.DESC2))));
    }

    @Test
    public void oneAuditedEntity_AllFieldsInCmd_NoFieldsChanged_ShouldReturnEmpty() {
        auditedPL.update(singletonList(new UpdateAuditedCommand(ID_1)
                                                 .with(AuditedType.NAME, "nameA")
                                                 .with(AuditedType.DESC, "descA")
                                                 .with(AuditedType.DESC2, "desc2A")),
                         auditedConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat(auditRecords, is(empty()));
    }

    @Test
    public void oneAuditedEntity_SomeFieldsInCmd_AllThoseChanged_ShouldCreateFieldRecordsForThem() {
        auditedPL.update(singletonList(new UpdateAuditedCommand(ID_1)
                                                 .with(AuditedType.NAME, "newNameA")
                                                 .with(AuditedType.DESC, "newDescA")),
                         auditedConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<AuditedType> auditRecord = typed(auditRecords.get(0));
        assertThat(auditRecord, allOf(hasEntityType(AuditedType.INSTANCE),
                                      hasEntityId(String.valueOf(ID_1)),
                                      hasOperator(UPDATE),
                                      hasChangedFieldRecord(AuditedType.NAME, "nameA", "newNameA"),
                                      hasChangedFieldRecord(AuditedType.DESC, "descA", "newDescA"),
                                      not(hasFieldRecordFor(AuditedType.DESC2))));
    }

    @Test
    public void oneAuditedEntity_DoesntExist_ShouldReturnEmpty() {
        auditedPL.update(singletonList(new UpdateAuditedCommand(INVALID_ID)),
                         auditedConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat(auditRecords, is(empty()));
    }

    @Test
    public void twoAuditedEntities_AllFieldsChanged_ShouldCreateFieldRecordsForAll() {
        final List<UpdateAuditedCommand> cmds =
            ImmutableList.of(new UpdateAuditedCommand(ID_1)
                                 .with(AuditedType.NAME, "newNameA")
                                 .with(AuditedType.DESC, "newDescA")
                                 .with(AuditedType.DESC2, "newDesc2A"),
                             new UpdateAuditedCommand(ID_2)
                                 .with(AuditedType.NAME, "newNameB")
                                 .with(AuditedType.DESC, "newDescB")
                                 .with(AuditedType.DESC2, "newDesc2B"));

        auditedPL.update(cmds, auditedConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(2));

        final AuditRecord<AuditedType> auditRecord1 = typed(auditRecords.get(0));
        assertThat(auditRecord1, allOf(hasEntityType(AuditedType.INSTANCE),
                                       hasEntityId(String.valueOf(ID_1)),
                                       hasOperator(UPDATE),
                                       hasChangedFieldRecord(AuditedType.NAME, "nameA", "newNameA"),
                                       hasChangedFieldRecord(AuditedType.DESC, "descA", "newDescA"),
                                       hasChangedFieldRecord(AuditedType.DESC2, "desc2A", "newDesc2A")));

        final AuditRecord<AuditedType> auditRecord2 = typed(auditRecords.get(1));
        assertThat(auditRecord2, allOf(hasEntityType(AuditedType.INSTANCE),
                                       hasEntityId(String.valueOf(ID_2)),
                                       hasOperator(UPDATE),
                                       hasChangedFieldRecord(AuditedType.NAME, "nameB", "newNameB"),
                                       hasChangedFieldRecord(AuditedType.DESC, "descB", "newDescB"),
                                       hasChangedFieldRecord(AuditedType.DESC2, "desc2B", "newDesc2B")));
    }

    @Test
    public void twoAuditedEntities_OnlyOneExists_ShouldCreateOnlyOneRecord() {
        final List<UpdateAuditedCommand> cmds =
            ImmutableList.of(new UpdateAuditedCommand(ID_1)
                                 .with(AuditedType.NAME, "newNameA"),
                             new UpdateAuditedCommand(INVALID_ID)
                                 .with(AuditedType.NAME, "newNameB"));

        auditedPL.update(cmds, auditedConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));

        final AuditRecord<AuditedType> auditRecord1 = typed(auditRecords.get(0));
        assertThat(auditRecord1, allOf(hasEntityType(AuditedType.INSTANCE),
                                       hasEntityId(String.valueOf(ID_1)),
                                       hasOperator(UPDATE)));
    }

    @Test
    public void oneInclusiveAuditedEntity_AllFieldsChanged_ShouldCreateFieldRecordsForAuditedOnly() {
        inclusiveAuditedPL.update(singletonList(new UpdateInclusiveAuditedCommand(ID_1)
                                                 .with(InclusiveAuditedType.NAME, "newNameA")
                                                 .with(InclusiveAuditedType.DESC, "newDescA")
                                                 .with(InclusiveAuditedType.DESC2, "newDesc2A")),
                                  inclusiveAuditedConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<InclusiveAuditedType> auditRecord = typed(auditRecords.get(0));
        assertThat(auditRecord, allOf(hasEntityType(InclusiveAuditedType.INSTANCE),
                                      hasEntityId(String.valueOf(ID_1)),
                                      hasOperator(UPDATE),
                                      hasChangedFieldRecord(InclusiveAuditedType.NAME, "nameA", "newNameA"),
                                      hasChangedFieldRecord(InclusiveAuditedType.DESC, "descA", "newDescA"),
                                      not(hasFieldRecordFor(InclusiveAuditedType.DESC2))));
    }

    @Test
    public void oneInclusiveAuditedEntity_PartiallyIntersectCmd_FieldsChanged_ShouldCreateFieldRecordsForIntersectionOnly() {
        inclusiveAuditedPL.update(singletonList(new UpdateInclusiveAuditedCommand(ID_1)
                                                           .with(InclusiveAuditedType.DESC, "newDescA")),
                                  inclusiveAuditedConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<InclusiveAuditedType> auditRecord = typed(auditRecords.get(0));
        assertThat(auditRecord, allOf(hasEntityType(InclusiveAuditedType.INSTANCE),
                                      hasEntityId(String.valueOf(ID_1)),
                                      hasOperator(UPDATE),
                                      not(hasFieldRecordFor(InclusiveAuditedType.NAME)),
                                      hasChangedFieldRecord(InclusiveAuditedType.DESC, "descA", "newDescA"),
                                      not(hasFieldRecordFor(InclusiveAuditedType.DESC2))));
    }

    @Test
    public void oneAuditedEntityWithoutDataFields_Exists_ShouldReturnEmpty() {
        auditedWithoutDataFieldsPL.update(singletonList(new UpdateAuditedWithoutDataFieldsCommand(ID_1)),
                                          auditedWithoutDataFieldsConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat(auditRecords, is(empty()));
    }

    @Test
    public void oneNotAuditedEntity_FieldsChanged_ShouldReturnEmpty() {
        notAuditedPL.update(singletonList(new UpdateNotAuditedCommand(ID_1)
                                                    .with(NotAuditedType.NAME, "newNameA")),
                            notAuditedConfig);

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
