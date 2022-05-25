package com.kenshoo.pl.audit;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.jooq.DataTableUtils;
import com.kenshoo.jooq.TestJooqConfig;
import com.kenshoo.pl.audit.commands.*;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.audit.AuditRecord;
import com.kenshoo.pl.entity.internal.audit.*;
import com.kenshoo.pl.entity.internal.audit.entitytypes.*;
import org.jooq.DSLContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static com.kenshoo.pl.entity.ChangeOperation.CREATE;
import static com.kenshoo.pl.entity.matchers.audit.AuditRecordMatchers.*;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class AuditForCreateTwoLevelsManualOrNoIdTest {

    private static final long PARENT_ID = 3;
    private static final long CHILD_ID = 33;
    private static final String PARENT_NAME = "parentName";
    private static final String CHILD_NAME = "childName";

    private static final List<? extends DataTable> ALL_TABLES =
            List.of(MainManualIdTable.INSTANCE,
                    MainWithoutIdTable.INSTANCE,
                    ChildManualIdTable.INSTANCE,
                    ChildWithoutIdOfParentManualIdTable.INSTANCE,
                    ChildWithoutIdOfParentWithoutIdTable.INSTANCE);

    private PLContext plContext;
    private DSLContext dslContext;
    private InMemoryAuditRecordPublisher auditRecordPublisher;

    private PersistenceLayer<AuditedManualIdType> parentWithManualIdPL;
    private PersistenceLayer<AuditedWithoutIdType> parentWithoutIdPL;

    @Before
    public void setUp() {
        dslContext = TestJooqConfig.create();
        auditRecordPublisher = new InMemoryAuditRecordPublisher();
        plContext = new PLContext.Builder(dslContext)
                .withFeaturePredicate(__ -> true)
                .withAuditRecordPublisher(auditRecordPublisher)
                .build();

        parentWithManualIdPL = persistenceLayer();
        parentWithoutIdPL = persistenceLayer();

        ALL_TABLES.forEach(table -> DataTableUtils.createTable(dslContext, table));
    }

    @After
    public void tearDown() {
        ALL_TABLES.forEach(table -> {
            try (final var dropTableStep = plContext.dslContext().dropTable(table)) {
                dropTableStep.execute();
            }
        });
    }


    @Test
    public void oneParentWithManualId_OneChildWithManualId_ShouldCreateRecordsForParentAndChild() {
        final var childCmd = new CreateAuditedChildManualIdCommand()
                .with(AuditedChildManualIdType.ID, CHILD_ID)
                .with(AuditedChildManualIdType.NAME, CHILD_NAME);
        final var parentCmd = new CreateAuditedManualIdTypeCommand()
                .with(AuditedManualIdType.ID, PARENT_ID)
                .with(AuditedManualIdType.NAME, PARENT_NAME)
                .with(childCmd);

        final var flowConfig = flowConfigBuilder(AuditedManualIdType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(AuditedChildManualIdType.INSTANCE))
                .build();

        parentWithManualIdPL.create(singletonList(parentCmd), flowConfig);

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                auditRecords, hasSize(1));
        final AuditRecord auditRecord = auditRecords.get(0);

        assertThat(auditRecord, allOf(hasEntityType(AuditedManualIdType.INSTANCE.getName()),
                hasEntityId(String.valueOf(PARENT_ID)),
                hasOperator(CREATE),
                hasCreatedFieldRecord(AuditedManualIdType.NAME, PARENT_NAME)));

        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(AuditedChildManualIdType.INSTANCE.getName()),
                hasEntityId(String.valueOf(CHILD_ID)),
                hasOperator(CREATE),
                hasCreatedFieldRecord(AuditedChildManualIdType.NAME, CHILD_NAME))));
    }

    @Ignore
    @Test
    public void oneParentWithManualId_OneChildWithoutId_ShouldCreateRecordsForParentAndChild() {
        final var childCmd = new CreateAuditedChildWithoutIdOfParentManualIdCommand()
                .with(AuditedChildWithoutIdOfParentManualIdType.NAME, CHILD_NAME);
        final var parentCmd = new CreateAuditedManualIdTypeCommand()
                .with(AuditedManualIdType.ID, PARENT_ID)
                .with(AuditedManualIdType.NAME, PARENT_NAME)
                .with(childCmd);

        final var flowConfig = flowConfigBuilder(AuditedManualIdType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(AuditedChildWithoutIdOfParentManualIdType.INSTANCE))
                .build();

        parentWithManualIdPL.create(singletonList(parentCmd), flowConfig);

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                auditRecords, hasSize(1));
        final AuditRecord auditRecord = auditRecords.get(0);

        assertThat(auditRecord, allOf(hasEntityType(AuditedManualIdType.INSTANCE.getName()),
                hasEntityId(String.valueOf(PARENT_ID)),
                hasOperator(CREATE),
                hasCreatedFieldRecord(AuditedManualIdType.NAME, PARENT_NAME)));

        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(AuditedChildWithoutIdOfParentManualIdType.INSTANCE.getName()),
                hasOperator(CREATE),
                hasMandatoryFieldValue(AuditedChildWithoutIdOfParentManualIdType.PARENT_ID, String.valueOf(PARENT_ID)),
                hasCreatedFieldRecord(AuditedChildWithoutIdOfParentManualIdType.NAME, CHILD_NAME))));
    }

    @Ignore
    @Test
    public void oneParentWithoutId_OneChildWithoutId_ShouldCreateRecordsForParentAndChild() {
        final var childCmd = new CreateAuditedChildWithoutIdOfParentWithoutIdCommand()
                .with(AuditedChildWithoutIdOfParentWithoutIdType.NAME, CHILD_NAME);
        final var parentCmd = new CreateAuditedWithoutIdTypeCommand()
                .with(AuditedWithoutIdType.NAME, PARENT_NAME)
                .with(childCmd);

        final var flowConfig = flowConfigBuilder(AuditedWithoutIdType.INSTANCE)
                .withChildFlowBuilder(flowConfigBuilder(AuditedChildWithoutIdOfParentWithoutIdType.INSTANCE))
                .build();

        parentWithoutIdPL.create(singletonList(parentCmd), flowConfig);

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                auditRecords, hasSize(1));
        final AuditRecord auditRecord = auditRecords.get(0);

        assertThat(auditRecord, allOf(hasEntityType(AuditedWithoutIdType.INSTANCE.getName()),
                hasOperator(CREATE),
                hasMandatoryFieldValue(AuditedWithoutIdType.NAME, PARENT_NAME),
                hasCreatedFieldRecord(AuditedWithoutIdType.NAME, PARENT_NAME)));

        assertThat(auditRecord, hasChildRecordThat(allOf(hasEntityType(AuditedChildWithoutIdOfParentWithoutIdType.INSTANCE.getName()),
                hasOperator(CREATE),
                hasMandatoryFieldValue(AuditedChildWithoutIdOfParentWithoutIdType.PARENT_NAME, String.valueOf(PARENT_NAME)),
                hasCreatedFieldRecord(AuditedChildWithoutIdOfParentWithoutIdType.NAME, CHILD_NAME))));
    }

    private <E extends EntityType<E>> ChangeFlowConfig.Builder<E> flowConfigBuilder(final E entityType) {
        return ChangeFlowConfigBuilderFactory.newInstance(plContext, entityType);
    }

    private <E extends EntityType<E>> PersistenceLayer<E> persistenceLayer() {
        return new PersistenceLayer<>(plContext);
    }
}
