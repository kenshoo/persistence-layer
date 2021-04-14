package com.kenshoo.pl.audit;

import com.google.common.collect.ImmutableList;
import com.kenshoo.jooq.DataTableUtils;
import com.kenshoo.jooq.TestJooqConfig;
import com.kenshoo.pl.audit.commands.DeleteAuditedCommand;
import com.kenshoo.pl.audit.commands.DeleteAuditedWithoutDataFieldsCommand;
import com.kenshoo.pl.audit.commands.DeleteNotAuditedCommand;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.audit.AuditRecord;
import com.kenshoo.pl.entity.internal.audit.MainTable;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithoutDataFieldsType;
import com.kenshoo.pl.entity.internal.audit.entitytypes.NotAuditedType;
import org.jooq.DSLContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.stream.Stream;

import static com.kenshoo.pl.entity.ChangeOperation.DELETE;
import static com.kenshoo.pl.entity.matchers.audit.AuditRecordMatchers.*;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class AuditForDeleteOneLevelTest {

    private static final long ID_1 = 1L;
    private static final long ID_2 = 2L;
    private static final long INVALID_ID = 999L;

    private PLContext plContext;
    private InMemoryAuditRecordPublisher auditRecordPublisher;

    private ChangeFlowConfig<AuditedType> auditedConfig;
    private ChangeFlowConfig<AuditedWithoutDataFieldsType> auditedWithoutDataFieldsConfig;
    private ChangeFlowConfig<NotAuditedType> notAuditedConfig;

    private PersistenceLayer<AuditedType> auditedPL;
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
        auditedWithoutDataFieldsConfig = flowConfig(AuditedWithoutDataFieldsType.INSTANCE);
        notAuditedConfig = flowConfig(NotAuditedType.INSTANCE);

        auditedPL = persistenceLayer();
        auditedWithoutDataFieldsPL = persistenceLayer();
        notAuditedPL = persistenceLayer();

        Stream.of(MainTable.INSTANCE)
              .forEach(table -> DataTableUtils.createTable(dslContext, table));

        dslContext.insertInto(MainTable.INSTANCE)
                  .set(MainTable.INSTANCE.id, ID_1)
                  .set(MainTable.INSTANCE.name, "name")
                  .execute();
        dslContext.insertInto(MainTable.INSTANCE)
                  .set(MainTable.INSTANCE.id, ID_2)
                  .set(MainTable.INSTANCE.name, "name2")
                  .execute();
    }

    @After
    public void tearDown() {
        Stream.of(MainTable.INSTANCE)
              .forEach(table -> plContext.dslContext().dropTable(table).execute());
    }

    @Test
    public void oneAuditedEntity_Exists_ShouldCreateRecordWithFixedData() {
        auditedPL.delete(singletonList(new DeleteAuditedCommand(ID_1)),
                         auditedConfig);

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord auditRecord = auditRecords.get(0);
        assertThat(auditRecord, allOf(hasEntityType(AuditedType.INSTANCE.getName()),
                                      hasEntityId(String.valueOf(ID_1)),
                                      hasOperator(DELETE)));
    }

    @Test
    public void oneAuditedEntity_DoesntExist_ShouldReturnEmpty() {
        auditedPL.delete(singletonList(new DeleteAuditedCommand(INVALID_ID)),
                         auditedConfig);

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat(auditRecords, is(empty()));
    }

    @Test
    public void twoAuditedEntities_BothExist_ShouldCreateTwoRecordsWithFixedData() {
        final List<DeleteAuditedCommand> cmds =
            ImmutableList.of(new DeleteAuditedCommand(ID_1),
                             new DeleteAuditedCommand(ID_2));

        auditedPL.delete(cmds, auditedConfig);

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(2));

        final AuditRecord auditRecord1 = auditRecords.get(0);
        assertThat(auditRecord1, allOf(hasEntityType(AuditedType.INSTANCE.getName()),
                                       hasEntityId(String.valueOf(ID_1)),
                                       hasOperator(DELETE)));

        final AuditRecord auditRecord2 = auditRecords.get(1);
        assertThat(auditRecord2, allOf(hasEntityType(AuditedType.INSTANCE.getName()),
                                       hasEntityId(String.valueOf(ID_2)),
                                       hasOperator(DELETE)));
    }

    @Test
    public void twoAuditedEntities_OnlyOneExists_ShouldCreateOnlyOneRecord() {
        final List<DeleteAuditedCommand> cmds =
            ImmutableList.of(new DeleteAuditedCommand(ID_1),
                             new DeleteAuditedCommand(INVALID_ID));

        auditedPL.delete(cmds, auditedConfig);

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));

        final AuditRecord auditRecord1 = auditRecords.get(0);
        assertThat(auditRecord1, allOf(hasEntityType(AuditedType.INSTANCE.getName()),
                                       hasEntityId(String.valueOf(ID_1)),
                                       hasOperator(DELETE)));
    }

    @Test
    public void oneAuditedEntityWithoutDataFields_Exists_ShouldCreateRecordWithFixedData() {
        auditedWithoutDataFieldsPL.delete(singletonList(new DeleteAuditedWithoutDataFieldsCommand(ID_1)),
                                          auditedWithoutDataFieldsConfig);

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord auditRecord = auditRecords.get(0);
        assertThat(auditRecord, allOf(hasEntityType(AuditedWithoutDataFieldsType.INSTANCE.getName()),
                                      hasEntityId(String.valueOf(ID_1)),
                                      hasOperator(DELETE)));
    }

    @Test
    public void oneNotAuditedEntity_Exists_ShouldReturnEmpty() {
        notAuditedPL.delete(singletonList(new DeleteNotAuditedCommand(ID_1)),
                            notAuditedConfig);

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat(auditRecords, is(empty()));
    }

    private <E extends EntityType<E>> ChangeFlowConfig<E> flowConfig(final E entityType) {
        return ChangeFlowConfigBuilderFactory.newInstance(plContext, entityType).build();
    }

    private <E extends EntityType<E>> PersistenceLayer<E> persistenceLayer() {
        return new PersistenceLayer<>(plContext);
    }
}
