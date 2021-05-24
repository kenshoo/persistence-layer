package com.kenshoo.pl.audit;

import com.kenshoo.jooq.DataTableUtils;
import com.kenshoo.jooq.TestJooqConfig;
import com.kenshoo.pl.audit.commands.CreateAuditedCommand;
import com.kenshoo.pl.entity.ChangeFlowConfigBuilderFactory;
import com.kenshoo.pl.entity.PLContext;
import com.kenshoo.pl.entity.PersistenceLayer;
import com.kenshoo.pl.entity.audit.AuditRecord;
import com.kenshoo.pl.entity.internal.audit.MainTable;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType;
import org.jooq.DSLContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;

public class AuditDisabledTest {

    private PLContext plContext;

    private InMemoryAuditRecordPublisher auditRecordPublisher;

    private PersistenceLayer<AuditedType> pl;

    @Before
    public void setUp() {
        final DSLContext dslContext = TestJooqConfig.create();
        auditRecordPublisher = new InMemoryAuditRecordPublisher();
        plContext = new PLContext.Builder(dslContext)
            .withFeaturePredicate(__ -> true)
            .withAuditRecordPublisher(auditRecordPublisher)
            .build();

        pl = new PersistenceLayer<>(plContext);

        DataTableUtils.createTable(dslContext, MainTable.INSTANCE);
    }

    @After
    public void tearDown() {
        plContext.dslContext().dropTable(MainTable.INSTANCE).execute();
    }

    @Test
    public void shouldNotGenerateAuditRecordWhenAuditingDisabled() {
        var flowConfig = ChangeFlowConfigBuilderFactory.newInstance(plContext, AuditedType.INSTANCE)
                                                       .disableAuditing()
                                                       .build();

        pl.create(singletonList(new CreateAuditedCommand()
                                    .with(AuditedType.NAME, "name")),
                  flowConfig);

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("There should be no published records",
                   auditRecords, empty());
    }
}
