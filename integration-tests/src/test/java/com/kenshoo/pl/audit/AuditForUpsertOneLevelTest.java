package com.kenshoo.pl.audit;

import com.google.common.collect.ImmutableList;
import com.kenshoo.jooq.DataTableUtils;
import com.kenshoo.jooq.TestJooqConfig;
import com.kenshoo.pl.audit.commands.UpsertAuditedCommand;
import com.kenshoo.pl.audit.commands.UpsertNotAuditedCommand;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.audit.AuditRecord;
import com.kenshoo.pl.entity.internal.audit.MainAutoIncIdTable;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedAutoIncIdType;
import com.kenshoo.pl.entity.internal.audit.entitytypes.NotAuditedType;
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

    private ChangeFlowConfig<AuditedAutoIncIdType> auditedConfig;
    private ChangeFlowConfig<NotAuditedType> notAuditedConfig;

    private PersistenceLayer<AuditedAutoIncIdType> auditedPL;
    private PersistenceLayer<NotAuditedType> notAuditedPL;

    @Before
    public void setUp() {
        dslContext = TestJooqConfig.create();
        auditRecordPublisher = new InMemoryAuditRecordPublisher();
        plContext = new PLContext.Builder(dslContext)
            .withFeaturePredicate(__ -> true)
            .withAuditRecordPublisher(auditRecordPublisher)
            .build();

        auditedConfig = flowConfig(AuditedAutoIncIdType.INSTANCE);
        notAuditedConfig = flowConfig(NotAuditedType.INSTANCE);

        auditedPL = persistenceLayer();
        notAuditedPL = persistenceLayer();

        Stream.of(MainAutoIncIdTable.INSTANCE)
              .forEach(table -> DataTableUtils.createTable(dslContext, table));

        dslContext.insertInto(MainAutoIncIdTable.INSTANCE)
                  .set(MainAutoIncIdTable.INSTANCE.id, EXISTING_ID_1)
                  .set(MainAutoIncIdTable.INSTANCE.name, EXISTING_NAME_1)
                  .set(MainAutoIncIdTable.INSTANCE.desc, "descA")
                  .set(MainAutoIncIdTable.INSTANCE.desc2, "desc2A")
                  .execute();
        dslContext.insertInto(MainAutoIncIdTable.INSTANCE)
                  .set(MainAutoIncIdTable.INSTANCE.id, EXISTING_ID_2)
                  .set(MainAutoIncIdTable.INSTANCE.name, EXISTING_NAME_2)
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
    public void oneAuditedEntity_New_ShouldReturnCreateOperator_AndCreatedFieldRecords() {
        auditedPL.upsert(singletonList(new UpsertAuditedCommand(NEW_NAME_1)
                                           .with(AuditedAutoIncIdType.DESC, "desc")
                                           .with(AuditedAutoIncIdType.DESC2, "desc2")),
                         auditedConfig);

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord auditRecord = auditRecords.get(0);
        assertThat(auditRecord, allOf(hasEntityType(AuditedAutoIncIdType.INSTANCE.getName()),
                                      hasEntityId(String.valueOf(fetchIdByName(NEW_NAME_1))),
                                      hasOperator(CREATE),
                                      hasCreatedFieldRecord(AuditedAutoIncIdType.NAME, NEW_NAME_1),
                                      hasCreatedFieldRecord(AuditedAutoIncIdType.DESC, "desc"),
                                      hasCreatedFieldRecord(AuditedAutoIncIdType.DESC2, "desc2")));
    }

    @Test
    public void oneAuditedEntity_Existing_ShouldReturnUpdateOperator_AndChangedFieldRecordsExceptKey() {
        auditedPL.upsert(singletonList(new UpsertAuditedCommand(EXISTING_NAME_1)
                                           .with(AuditedAutoIncIdType.DESC, "newDescA")
                                           .with(AuditedAutoIncIdType.DESC2, "newDesc2A")),
                         auditedConfig);

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord auditRecord = auditRecords.get(0);
        assertThat(auditRecord, allOf(hasEntityType(AuditedAutoIncIdType.INSTANCE.getName()),
                                      hasEntityId(String.valueOf(EXISTING_ID_1)),
                                      hasOperator(UPDATE),
                                      not(hasFieldRecordFor(AuditedAutoIncIdType.NAME)),
                                      hasChangedFieldRecord(AuditedAutoIncIdType.DESC, "descA", "newDescA"),
                                      hasChangedFieldRecord(AuditedAutoIncIdType.DESC2, "desc2A", "newDesc2A")));
    }

    @Test
    public void oneAuditedEntity_Existing_NoFieldsChanged_ShouldReturnEmpty() {
        auditedPL.upsert(singletonList(new UpsertAuditedCommand(EXISTING_NAME_1)
                                           .with(AuditedAutoIncIdType.DESC, "descA")
                                           .with(AuditedAutoIncIdType.DESC2, "desc2A")),
                         auditedConfig);

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat(auditRecords, is(empty()));
    }

    @Test
    public void twoAuditedEntities_BothNew_ShouldReturnCreateOperator_AndCreatedFieldRecords() {
        final List<UpsertAuditedCommand> cmds =
            ImmutableList.of(new UpsertAuditedCommand(NEW_NAME_1)
                                 .with(AuditedAutoIncIdType.DESC, "newDescA")
                                 .with(AuditedAutoIncIdType.DESC2, "newDesc2A"),
                             new UpsertAuditedCommand(NEW_NAME_2)
                                 .with(AuditedAutoIncIdType.DESC, "newDescB")
                                 .with(AuditedAutoIncIdType.DESC2, "newDesc2B"));

        auditedPL.upsert(cmds, auditedConfig);

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(2));

        final AuditRecord auditRecord1 = auditRecords.get(0);
        assertThat(auditRecord1, allOf(hasEntityType(AuditedAutoIncIdType.INSTANCE.getName()),
                                       hasEntityId(String.valueOf(fetchIdByName(NEW_NAME_1))),
                                       hasOperator(CREATE),
                                       hasCreatedFieldRecord(AuditedAutoIncIdType.NAME, NEW_NAME_1),
                                       hasCreatedFieldRecord(AuditedAutoIncIdType.DESC, "newDescA"),
                                       hasCreatedFieldRecord(AuditedAutoIncIdType.DESC2, "newDesc2A")));

        final AuditRecord auditRecord2 = auditRecords.get(1);
        assertThat(auditRecord2, allOf(hasEntityType(AuditedAutoIncIdType.INSTANCE.getName()),
                                       hasEntityId(String.valueOf(fetchIdByName(NEW_NAME_2))),
                                       hasOperator(CREATE),
                                       hasCreatedFieldRecord(AuditedAutoIncIdType.NAME, NEW_NAME_2),
                                       hasCreatedFieldRecord(AuditedAutoIncIdType.DESC, "newDescB"),
                                       hasCreatedFieldRecord(AuditedAutoIncIdType.DESC2, "newDesc2B")));
    }

    @Test
    public void twoAuditedEntities_BothExisting_ShouldReturnUpdateOperator_AndChangedFieldRecordsExceptKey() {
        final List<UpsertAuditedCommand> cmds =
            ImmutableList.of(new UpsertAuditedCommand(EXISTING_NAME_1)
                                 .with(AuditedAutoIncIdType.DESC, "newDescA")
                                 .with(AuditedAutoIncIdType.DESC2, "newDesc2A"),
                             new UpsertAuditedCommand(EXISTING_NAME_2)
                                 .with(AuditedAutoIncIdType.DESC, "newDescB")
                                 .with(AuditedAutoIncIdType.DESC2, "newDesc2B"));

        auditedPL.upsert(cmds, auditedConfig);

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(2));

        final AuditRecord auditRecord1 = auditRecords.get(0);
        assertThat(auditRecord1, allOf(hasEntityType(AuditedAutoIncIdType.INSTANCE.getName()),
                                       hasEntityId(String.valueOf(EXISTING_ID_1)),
                                       hasOperator(UPDATE),
                                       not(hasFieldRecordFor(AuditedAutoIncIdType.NAME)),
                                       hasChangedFieldRecord(AuditedAutoIncIdType.DESC, "descA", "newDescA"),
                                       hasChangedFieldRecord(AuditedAutoIncIdType.DESC2, "desc2A", "newDesc2A")));

        final AuditRecord auditRecord2 = auditRecords.get(1);
        assertThat(auditRecord2, allOf(hasEntityType(AuditedAutoIncIdType.INSTANCE.getName()),
                                       hasEntityId(String.valueOf(EXISTING_ID_2)),
                                       hasOperator(UPDATE),
                                       not(hasFieldRecordFor(AuditedAutoIncIdType.NAME)),
                                       hasChangedFieldRecord(AuditedAutoIncIdType.DESC, "descB", "newDescB"),
                                       hasChangedFieldRecord(AuditedAutoIncIdType.DESC2, "desc2B", "newDesc2B")));
    }

    @Test
    public void twoAuditedEntities_OneNew_OneExistingWithChanges_ShouldReturnProperOperators_AndFieldRecords() {
        final List<UpsertAuditedCommand> cmds =
            ImmutableList.of(new UpsertAuditedCommand(NEW_NAME_1)
                                 .with(AuditedAutoIncIdType.DESC, "newDescA")
                                 .with(AuditedAutoIncIdType.DESC2, "newDesc2A"),
                             new UpsertAuditedCommand(EXISTING_NAME_2)
                                 .with(AuditedAutoIncIdType.DESC, "newDescB")
                                 .with(AuditedAutoIncIdType.DESC2, "newDesc2B"));

        auditedPL.upsert(cmds, auditedConfig);

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(2));

        final AuditRecord auditRecord1 = auditRecords.get(0);
        assertThat(auditRecord1, allOf(hasEntityType(AuditedAutoIncIdType.INSTANCE.getName()),
                                       hasEntityId(String.valueOf(fetchIdByName(NEW_NAME_1))),
                                       hasOperator(CREATE),
                                       hasCreatedFieldRecord(AuditedAutoIncIdType.NAME, NEW_NAME_1),
                                       hasCreatedFieldRecord(AuditedAutoIncIdType.DESC, "newDescA"),
                                       hasCreatedFieldRecord(AuditedAutoIncIdType.DESC2, "newDesc2A")));

        final AuditRecord auditRecord2 = auditRecords.get(1);
        assertThat(auditRecord2, allOf(hasEntityType(AuditedAutoIncIdType.INSTANCE.getName()),
                                       hasEntityId(String.valueOf(EXISTING_ID_2)),
                                       hasOperator(UPDATE),
                                       not(hasFieldRecordFor(AuditedAutoIncIdType.NAME)),
                                       hasChangedFieldRecord(AuditedAutoIncIdType.DESC, "descB", "newDescB"),
                                       hasChangedFieldRecord(AuditedAutoIncIdType.DESC2, "desc2B", "newDesc2B")));
    }

    @Test
    public void oneNotAuditedEntity_New_ShouldReturnEmpty() {
        notAuditedPL.upsert(singletonList(new UpsertNotAuditedCommand("name")),
                            notAuditedConfig);

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat(auditRecords, is(empty()));
    }

    @Test
    public void oneNotAuditedEntity_Existing_FieldsChanged_ShouldReturnEmpty() {
        notAuditedPL.upsert(singletonList(new UpsertNotAuditedCommand(EXISTING_NAME_1)
                                              .with(NotAuditedType.DESC, "newDescA")),
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

    private long fetchIdByName(final String name) {
        return dslContext.select(MainAutoIncIdTable.INSTANCE.id)
                         .from(MainAutoIncIdTable.INSTANCE)
                         .where(MainAutoIncIdTable.INSTANCE.name.eq(name))
                         .fetchOptional(MainAutoIncIdTable.INSTANCE.id)
                         .orElseThrow(() -> new IllegalStateException("Could not fetch the id of the entity named '" + name + "'"));
    }
}
