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
import com.kenshoo.pl.entity.internal.audit.MainAutoIncIdTable;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedAutoIncIdType;
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

    private ChangeFlowConfig<AuditedAutoIncIdType> auditedConfig;
    private ChangeFlowConfig<InclusiveAuditedType> inclusiveAuditedConfig;
    private ChangeFlowConfig<AuditedWithoutDataFieldsType> auditedWithoutDataFieldsConfig;
    private ChangeFlowConfig<NotAuditedType> notAuditedConfig;

    private PersistenceLayer<AuditedAutoIncIdType> auditedPL;
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

        auditedConfig = flowConfig(AuditedAutoIncIdType.INSTANCE);
        inclusiveAuditedConfig = flowConfig(InclusiveAuditedType.INSTANCE);
        auditedWithoutDataFieldsConfig = flowConfig(AuditedWithoutDataFieldsType.INSTANCE);
        notAuditedConfig = flowConfig(NotAuditedType.INSTANCE);

        auditedPL = persistenceLayer();
        inclusiveAuditedPL = persistenceLayer();
        auditedWithoutDataFieldsPL = persistenceLayer();
        notAuditedPL = persistenceLayer();

        Stream.of(MainAutoIncIdTable.INSTANCE)
              .forEach(table -> DataTableUtils.createTable(dslContext, table));

        dslContext.insertInto(MainAutoIncIdTable.INSTANCE)
                  .set(MainAutoIncIdTable.INSTANCE.id, ID_1)
                  .set(MainAutoIncIdTable.INSTANCE.name, "nameA")
                  .set(MainAutoIncIdTable.INSTANCE.desc, "descA")
                  .set(MainAutoIncIdTable.INSTANCE.desc2, "desc2A")
                  .execute();
        dslContext.insertInto(MainAutoIncIdTable.INSTANCE)
                  .set(MainAutoIncIdTable.INSTANCE.id, ID_2)
                  .set(MainAutoIncIdTable.INSTANCE.name, "nameB")
                  .set(MainAutoIncIdTable.INSTANCE.desc, "descB")
                  .set(MainAutoIncIdTable.INSTANCE.desc2, "desc2B")
                  .execute();
    }

    @After
    public void tearDown() {
        Stream.of(MainAutoIncIdTable.INSTANCE)
              .forEach(table -> plContext.dslContext().dropTable(table).execute());
    }

    @Test
    public void oneAuditedEntity_AllFieldsChanged_ShouldCreateFieldRecordsForAll() {
        auditedPL.update(singletonList(new UpdateAuditedCommand(ID_1)
                                                 .with(AuditedAutoIncIdType.NAME, "newNameA")
                                                 .with(AuditedAutoIncIdType.DESC, "newDescA")
                                                 .with(AuditedAutoIncIdType.DESC2, "newDesc2A")),
                         auditedConfig);

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord auditRecord = auditRecords.get(0);
        assertThat(auditRecord, allOf(hasEntityType(AuditedAutoIncIdType.INSTANCE.getName()),
                                      hasEntityId(String.valueOf(ID_1)),
                                      hasOperator(UPDATE),
                                      hasChangedFieldRecord(AuditedAutoIncIdType.NAME, "nameA", "newNameA"),
                                      hasChangedFieldRecord(AuditedAutoIncIdType.DESC, "descA", "newDescA"),
                                      hasChangedFieldRecord(AuditedAutoIncIdType.DESC2, "desc2A", "newDesc2A")));
    }

    @Test
    public void oneAuditedEntity_AllFieldsInCmd_SomeFieldsChanged_ShouldCreateFieldRecordsForChangedOnly() {
        auditedPL.update(singletonList(new UpdateAuditedCommand(ID_1)
                                                 .with(AuditedAutoIncIdType.NAME, "newNameA")
                                                 .with(AuditedAutoIncIdType.DESC, "newDescA")
                                                 .with(AuditedAutoIncIdType.DESC2, "desc2A")),
                         auditedConfig);

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord auditRecord = auditRecords.get(0);
        assertThat(auditRecord, allOf(hasEntityType(AuditedAutoIncIdType.INSTANCE.getName()),
                                      hasEntityId(String.valueOf(ID_1)),
                                      hasOperator(UPDATE),
                                      hasChangedFieldRecord(AuditedAutoIncIdType.NAME, "nameA", "newNameA"),
                                      hasChangedFieldRecord(AuditedAutoIncIdType.DESC, "descA", "newDescA"),
                                      not(hasFieldRecordFor(AuditedAutoIncIdType.DESC2))));
    }

    @Test
    public void oneAuditedEntity_AllFieldsInCmd_NoFieldsChanged_ShouldReturnEmpty() {
        auditedPL.update(singletonList(new UpdateAuditedCommand(ID_1)
                                                 .with(AuditedAutoIncIdType.NAME, "nameA")
                                                 .with(AuditedAutoIncIdType.DESC, "descA")
                                                 .with(AuditedAutoIncIdType.DESC2, "desc2A")),
                         auditedConfig);

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat(auditRecords, is(empty()));
    }

    @Test
    public void oneAuditedEntity_SomeFieldsInCmd_AllThoseChanged_ShouldCreateFieldRecordsForThem() {
        auditedPL.update(singletonList(new UpdateAuditedCommand(ID_1)
                                                 .with(AuditedAutoIncIdType.NAME, "newNameA")
                                                 .with(AuditedAutoIncIdType.DESC, "newDescA")),
                         auditedConfig);

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord auditRecord = auditRecords.get(0);
        assertThat(auditRecord, allOf(hasEntityType(AuditedAutoIncIdType.INSTANCE.getName()),
                                      hasEntityId(String.valueOf(ID_1)),
                                      hasOperator(UPDATE),
                                      hasChangedFieldRecord(AuditedAutoIncIdType.NAME, "nameA", "newNameA"),
                                      hasChangedFieldRecord(AuditedAutoIncIdType.DESC, "descA", "newDescA"),
                                      not(hasFieldRecordFor(AuditedAutoIncIdType.DESC2))));
    }

    @Test
    public void oneAuditedEntity_DoesntExist_ShouldReturnEmpty() {
        auditedPL.update(singletonList(new UpdateAuditedCommand(INVALID_ID)),
                         auditedConfig);

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat(auditRecords, is(empty()));
    }

    @Test
    public void twoAuditedEntities_AllFieldsChanged_ShouldCreateFieldRecordsForAll() {
        final List<UpdateAuditedCommand> cmds =
            ImmutableList.of(new UpdateAuditedCommand(ID_1)
                                 .with(AuditedAutoIncIdType.NAME, "newNameA")
                                 .with(AuditedAutoIncIdType.DESC, "newDescA")
                                 .with(AuditedAutoIncIdType.DESC2, "newDesc2A"),
                             new UpdateAuditedCommand(ID_2)
                                 .with(AuditedAutoIncIdType.NAME, "newNameB")
                                 .with(AuditedAutoIncIdType.DESC, "newDescB")
                                 .with(AuditedAutoIncIdType.DESC2, "newDesc2B"));

        auditedPL.update(cmds, auditedConfig);

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(2));

        final AuditRecord auditRecord1 = auditRecords.get(0);
        assertThat(auditRecord1, allOf(hasEntityType(AuditedAutoIncIdType.INSTANCE.getName()),
                                       hasEntityId(String.valueOf(ID_1)),
                                       hasOperator(UPDATE),
                                       hasChangedFieldRecord(AuditedAutoIncIdType.NAME, "nameA", "newNameA"),
                                       hasChangedFieldRecord(AuditedAutoIncIdType.DESC, "descA", "newDescA"),
                                       hasChangedFieldRecord(AuditedAutoIncIdType.DESC2, "desc2A", "newDesc2A")));

        final AuditRecord auditRecord2 = auditRecords.get(1);
        assertThat(auditRecord2, allOf(hasEntityType(AuditedAutoIncIdType.INSTANCE.getName()),
                                       hasEntityId(String.valueOf(ID_2)),
                                       hasOperator(UPDATE),
                                       hasChangedFieldRecord(AuditedAutoIncIdType.NAME, "nameB", "newNameB"),
                                       hasChangedFieldRecord(AuditedAutoIncIdType.DESC, "descB", "newDescB"),
                                       hasChangedFieldRecord(AuditedAutoIncIdType.DESC2, "desc2B", "newDesc2B")));
    }

    @Test
    public void twoAuditedEntities_OnlyOneExists_ShouldCreateOnlyOneRecord() {
        final List<UpdateAuditedCommand> cmds =
            ImmutableList.of(new UpdateAuditedCommand(ID_1)
                                 .with(AuditedAutoIncIdType.NAME, "newNameA"),
                             new UpdateAuditedCommand(INVALID_ID)
                                 .with(AuditedAutoIncIdType.NAME, "newNameB"));

        auditedPL.update(cmds, auditedConfig);

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));

        final AuditRecord auditRecord1 = auditRecords.get(0);
        assertThat(auditRecord1, allOf(hasEntityType(AuditedAutoIncIdType.INSTANCE.getName()),
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

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord auditRecord = auditRecords.get(0);
        assertThat(auditRecord, allOf(hasEntityType(InclusiveAuditedType.INSTANCE.getName()),
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

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord auditRecord = auditRecords.get(0);
        assertThat(auditRecord, allOf(hasEntityType(InclusiveAuditedType.INSTANCE.getName()),
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

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat(auditRecords, is(empty()));
    }

    @Test
    public void oneNotAuditedEntity_FieldsChanged_ShouldReturnEmpty() {
        notAuditedPL.update(singletonList(new UpdateNotAuditedCommand(ID_1)
                                                    .with(NotAuditedType.NAME, "newNameA")),
                            notAuditedConfig);

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("There should not be any published audit records",
                   auditRecords, is(empty()));
    }

    private <E extends EntityType<E>> ChangeFlowConfig<E> flowConfig(final E entityType) {
        return ChangeFlowConfigBuilderFactory.newInstance(plContext, entityType).build();
    }

    private <E extends EntityType<E>> PersistenceLayer<E> persistenceLayer() {
        return new PersistenceLayer<>(plContext);
    }
}
