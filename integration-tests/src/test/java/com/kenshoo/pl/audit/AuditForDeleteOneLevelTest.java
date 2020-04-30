package com.kenshoo.pl.audit;

import com.google.common.collect.ImmutableList;
import com.kenshoo.jooq.DataTableUtils;
import com.kenshoo.jooq.TestJooqConfig;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.audit.TestAuditedEntityType;
import com.kenshoo.pl.entity.internal.audit.TestAuditedEntityWithoutDataFieldsType;
import com.kenshoo.pl.entity.internal.audit.TestEntityTable;
import com.kenshoo.pl.entity.internal.audit.TestEntityType;
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

    private ChangeFlowConfig<TestAuditedEntityType> auditedEntityConfig;
    private ChangeFlowConfig<TestAuditedEntityWithoutDataFieldsType> auditedEntityWithoutDataFieldsConfig;
    private ChangeFlowConfig<TestEntityType> notAuditedEntityConfig;

    private PersistenceLayer<TestAuditedEntityType> auditedEntityPL;
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
        auditedEntityWithoutDataFieldsConfig = flowConfig(TestAuditedEntityWithoutDataFieldsType.INSTANCE);
        notAuditedEntityConfig = flowConfig(TestEntityType.INSTANCE);

        auditedEntityPL = persistenceLayer();
        auditedEntityWithoutDataFieldsPL = persistenceLayer();
        notAuditedEntityPL = persistenceLayer();

        Stream.of(TestEntityTable.INSTANCE)
              .forEach(table -> DataTableUtils.createTable(dslContext, table));

        dslContext.insertInto(TestEntityTable.INSTANCE)
                  .set(TestEntityTable.INSTANCE.id, ID_1)
                  .set(TestEntityTable.INSTANCE.name, "name")
                  .execute();
        dslContext.insertInto(TestEntityTable.INSTANCE)
                  .set(TestEntityTable.INSTANCE.id, ID_2)
                  .set(TestEntityTable.INSTANCE.name, "name2")
                  .execute();
    }

    @After
    public void tearDown() {
        Stream.of(TestEntityTable.INSTANCE)
              .forEach(table -> plContext.dslContext().dropTable(table).execute());
    }

    @Test
    public void oneAuditedEntity_Exists_ShouldCreateRecordWithFixedData() {
        auditedEntityPL.delete(singletonList(new DeleteTestAuditedEntityCommand(ID_1)),
                               auditedEntityConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<TestAuditedEntityType> auditRecord = typed(auditRecords.get(0));
        assertThat(auditRecord, allOf(hasEntityType(TestAuditedEntityType.INSTANCE),
                                      hasEntityId(String.valueOf(ID_1)),
                                      hasOperator(DELETE)));
    }

    @Test
    public void oneAuditedEntity_DoesntExist_ShouldReturnEmpty() {
        auditedEntityPL.delete(singletonList(new DeleteTestAuditedEntityCommand(INVALID_ID)),
                               auditedEntityConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat(auditRecords, is(empty()));
    }

    @Test
    public void twoAuditedEntities_BothExist_ShouldCreateTwoRecordsWithFixedData() {
        final List<DeleteTestAuditedEntityCommand> cmds =
            ImmutableList.of(new DeleteTestAuditedEntityCommand(ID_1),
                             new DeleteTestAuditedEntityCommand(ID_2));

        auditedEntityPL.delete(cmds, auditedEntityConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(2));

        final AuditRecord<TestAuditedEntityType> auditRecord1 = typed(auditRecords.get(0));
        assertThat(auditRecord1, allOf(hasEntityType(TestAuditedEntityType.INSTANCE),
                                       hasEntityId(String.valueOf(ID_1)),
                                       hasOperator(DELETE)));

        final AuditRecord<TestAuditedEntityType> auditRecord2 = typed(auditRecords.get(1));
        assertThat(auditRecord2, allOf(hasEntityType(TestAuditedEntityType.INSTANCE),
                                       hasEntityId(String.valueOf(ID_2)),
                                       hasOperator(DELETE)));
    }

    @Test
    public void twoAuditedEntities_OnlyOneExists_ShouldCreateOnlyOneRecord() {
        final List<DeleteTestAuditedEntityCommand> cmds =
            ImmutableList.of(new DeleteTestAuditedEntityCommand(ID_1),
                             new DeleteTestAuditedEntityCommand(INVALID_ID));

        auditedEntityPL.delete(cmds, auditedEntityConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));

        final AuditRecord<TestAuditedEntityType> auditRecord1 = typed(auditRecords.get(0));
        assertThat(auditRecord1, allOf(hasEntityType(TestAuditedEntityType.INSTANCE),
                                       hasEntityId(String.valueOf(ID_1)),
                                       hasOperator(DELETE)));
    }

    @Test
    public void oneAuditedEntityWithoutDataFields_Exists_ShouldCreateRecordWithFixedData() {
        auditedEntityWithoutDataFieldsPL.delete(singletonList(new DeleteTestAuditedEntityWithoutDataFieldsCommand(ID_1)),
                                                auditedEntityWithoutDataFieldsConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<TestAuditedEntityWithoutDataFieldsType> auditRecord = typed(auditRecords.get(0));
        assertThat(auditRecord, allOf(hasEntityType(TestAuditedEntityWithoutDataFieldsType.INSTANCE),
                                      hasEntityId(String.valueOf(ID_1)),
                                      hasOperator(DELETE)));
    }

    @Test
    public void oneNotAuditedEntity_Exists_ShouldReturnEmpty() {
        notAuditedEntityPL.delete(singletonList(new DeleteTestEntityCommand(ID_1)),
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

    @SuppressWarnings("unchecked")
    private <E extends EntityType<E>> AuditRecord<E> typed(final AuditRecord<?> auditRecord) {
        return (AuditRecord<E>) auditRecord;
    }
}
