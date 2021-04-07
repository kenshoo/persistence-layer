package com.kenshoo.pl.audit;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.jooq.DataTableUtils;
import com.kenshoo.jooq.TestJooqConfig;
import com.kenshoo.pl.audit.commands.CreateAuditedChild1WithNameOverrideCommand;
import com.kenshoo.pl.audit.commands.CreateAuditedChild2WithNameOverrideCommand;
import com.kenshoo.pl.audit.commands.CreateAuditedWithNameOverrideCommand;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.audit.AuditRecord;
import com.kenshoo.pl.entity.internal.audit.ChildTable;
import com.kenshoo.pl.entity.internal.audit.MainTable;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedChild1WithNameOverrideType;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedChild2WithNameOverrideType;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithNameOverrideType;
import org.jooq.DSLContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static com.kenshoo.pl.entity.matchers.audit.AuditRecordMatchers.hasChildRecordThat;
import static com.kenshoo.pl.entity.matchers.audit.AuditRecordMatchers.hasEntityType;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class AuditWithNameOverridesTest {

    private static final Set<DataTable> ALL_TABLES = Set.of(MainTable.INSTANCE, ChildTable.INSTANCE);

    private PLContext plContext;
    private InMemoryAuditRecordPublisher auditRecordPublisher;

    private ChangeFlowConfig<AuditedWithNameOverrideType> flowConfig;

    private PersistenceLayer<AuditedWithNameOverrideType> pl;

    @Before
    public void setUp() {
        final DSLContext dslContext = TestJooqConfig.create();
        auditRecordPublisher = new InMemoryAuditRecordPublisher();
        plContext = new PLContext.Builder(dslContext)
            .withFeaturePredicate(__ -> true)
            .withAuditRecordPublisher(auditRecordPublisher)
            .build();

        flowConfig = flowConfigBuilder(AuditedWithNameOverrideType.INSTANCE)
            .withChildFlowBuilder(flowConfigBuilder(AuditedChild1WithNameOverrideType.INSTANCE))
            .withChildFlowBuilder(flowConfigBuilder(AuditedChild2WithNameOverrideType.INSTANCE))
            .build();

        pl = persistenceLayer();

        ALL_TABLES.forEach(table -> DataTableUtils.createTable(dslContext, table));
    }

    @After
    public void tearDown() {
        ALL_TABLES.forEach(table -> plContext.dslContext().dropTable(table).execute());
    }

    @Test
    public void overrideNameInStandalone() {
        pl.create(singletonList(parentCreateCmd()), flowConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        assertThat(auditRecords.get(0), hasEntityType(AuditedWithNameOverrideType.NAME_OVERRIDE));
    }

    @Test
    public void overrideNameInParentAndChildren() {
        pl.create(singletonList(parentCreateCmd()
                                          .with(child1CreateCmd())
                                          .with(child2CreateCmd())),
                  flowConfig);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final var parentRecord = auditRecords.get(0);
        assertThat(parentRecord, hasEntityType(AuditedWithNameOverrideType.NAME_OVERRIDE));
        assertThat("Incorrect or missing child 1 type: ",
                   parentRecord, hasChildRecordThat(hasEntityType(AuditedChild1WithNameOverrideType.NAME_OVERRIDE)));
        assertThat("Incorrect or missing child 2 type: ",
                   parentRecord, hasChildRecordThat(hasEntityType(AuditedChild2WithNameOverrideType.NAME_OVERRIDE)));
    }

    private <E extends EntityType<E>> PersistenceLayer<E> persistenceLayer() {
        return new PersistenceLayer<>(plContext);
    }

    private <E extends EntityType<E>> ChangeFlowConfig.Builder<E> flowConfigBuilder(final E entityType) {
        return ChangeFlowConfigBuilderFactory.newInstance(plContext, entityType);
    }

    private CreateAuditedWithNameOverrideCommand parentCreateCmd() {
        return new CreateAuditedWithNameOverrideCommand()
            .with(AuditedWithNameOverrideType.DESC, "desc");
    }

    private CreateAuditedChild2WithNameOverrideCommand child2CreateCmd() {
        return new CreateAuditedChild2WithNameOverrideCommand()
            .with(AuditedChild2WithNameOverrideType.DESC, "child2Desc");
    }

    private CreateAuditedChild1WithNameOverrideCommand child1CreateCmd() {
        return new CreateAuditedChild1WithNameOverrideCommand()
            .with(AuditedChild1WithNameOverrideType.DESC, "child1Desc");
    }
}
