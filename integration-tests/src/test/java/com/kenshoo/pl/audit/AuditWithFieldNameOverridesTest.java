package com.kenshoo.pl.audit;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.jooq.DataTableUtils;
import com.kenshoo.jooq.TestJooqConfig;
import com.kenshoo.pl.audit.commands.CreateAuditedChild1WithFieldNameOverridesCommand;
import com.kenshoo.pl.audit.commands.CreateAuditedChild2WithFieldNameOverridesCommand;
import com.kenshoo.pl.audit.commands.CreateAuditedWithAncestorFieldNameOverridesCommand;
import com.kenshoo.pl.audit.commands.CreateAuditedWithFieldNameOverridesCommand;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.audit.AuditRecord;
import com.kenshoo.pl.entity.internal.audit.AncestorTable;
import com.kenshoo.pl.entity.internal.audit.ChildAutoIncIdTable;
import com.kenshoo.pl.entity.internal.audit.MainAutoIncIdTable;
import com.kenshoo.pl.entity.internal.audit.MainWithAncestorTable;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedChild1WithFieldNameOverridesType;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedChild2WithFieldNameOverridesType;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithAncestorFieldNameOverridesType;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithFieldNameOverridesType;
import org.jooq.DSLContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static com.kenshoo.pl.entity.internal.audit.ancestorfieldsproviders.AncestorWithFieldNameOverridesAuditExtensions.DESC_FIELD_NAME_OVERRIDE;
import static com.kenshoo.pl.entity.internal.audit.ancestorfieldsproviders.AncestorWithFieldNameOverridesAuditExtensions.NAME_FIELD_NAME_OVERRIDE;
import static com.kenshoo.pl.entity.matchers.audit.AuditRecordMatchers.*;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class AuditWithFieldNameOverridesTest {

    private static final Set<DataTable> ALL_TABLES = Set.of(AncestorTable.INSTANCE,
                                                            MainWithAncestorTable.INSTANCE,
                                                            MainAutoIncIdTable.INSTANCE,
                                                            ChildAutoIncIdTable.INSTANCE);

    private static final String ANCESTOR_NAME_VALUE = "ancestorNameValue";
    private static final String ANCESTOR_DESC_VALUE = "ancestorDescValue";
    private static final long ANCESTOR_ID = 123;


    private PLContext plContext;
    private InMemoryAuditRecordPublisher auditRecordPublisher;

    private ChangeFlowConfig<AuditedWithFieldNameOverridesType> mainFlowConfig;
    private ChangeFlowConfig<AuditedWithAncestorFieldNameOverridesType> mainWithAncestorFlowConfig;

    private PersistenceLayer<AuditedWithFieldNameOverridesType> mainPL;
    private PersistenceLayer<AuditedWithAncestorFieldNameOverridesType> mainWithAncestorPL;

    @Before
    public void setUp() {
        final DSLContext dslContext = TestJooqConfig.create();
        auditRecordPublisher = new InMemoryAuditRecordPublisher();
        plContext = new PLContext.Builder(dslContext)
            .withFeaturePredicate(__ -> true)
            .withAuditRecordPublisher(auditRecordPublisher)
            .build();

        mainFlowConfig = flowConfigBuilder(AuditedWithFieldNameOverridesType.INSTANCE)
            .withChildFlowBuilder(flowConfigBuilder(AuditedChild1WithFieldNameOverridesType.INSTANCE))
            .withChildFlowBuilder(flowConfigBuilder(AuditedChild2WithFieldNameOverridesType.INSTANCE))
            .build();
        mainWithAncestorFlowConfig = flowConfigBuilder(AuditedWithAncestorFieldNameOverridesType.INSTANCE).build();

        mainPL = persistenceLayer();
        mainWithAncestorPL = persistenceLayer();

        ALL_TABLES.forEach(table -> DataTableUtils.createTable(dslContext, table));

        dslContext.insertInto(AncestorTable.INSTANCE)
                  .set(AncestorTable.INSTANCE.id, ANCESTOR_ID)
                  .set(AncestorTable.INSTANCE.name, ANCESTOR_NAME_VALUE)
                  .set(AncestorTable.INSTANCE.desc, ANCESTOR_DESC_VALUE)
                  .execute();
    }

    @After
    public void tearDown() {
        ALL_TABLES.forEach(table -> plContext.dslContext().dropTable(table).execute());
    }

    @Test
    public void overrideFieldNameInStandalone() {
        final String descValue = "descValue";

        mainPL.create(singletonList(parentCreateCmd()
                                        .with(AuditedWithFieldNameOverridesType.DESC, descValue)), mainFlowConfig);

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        assertThat(auditRecords.get(0), hasCreatedFieldRecord(AuditedWithFieldNameOverridesType.DESC_FIELD_NAME_OVERRIDE,
                                                              descValue));
    }

    @Test
    public void overrideFieldNameInParentAndChildren() {
        final String parentDescValue = "parentDescValue";
        final String child1DescValue = "child1DescValue";
        final String child2DescValue = "child2DescValue";

        mainPL.create(singletonList(parentCreateCmd().with(AuditedWithFieldNameOverridesType.DESC, parentDescValue)
                                                     .with(child1CreateCmd().with(AuditedChild1WithFieldNameOverridesType.DESC, child1DescValue))
                                                     .with(child2CreateCmd().with(AuditedChild2WithFieldNameOverridesType.DESC, child2DescValue))),
                      mainFlowConfig);

        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final var parentRecord = auditRecords.get(0);
        assertThat(parentRecord, hasCreatedFieldRecord(AuditedWithFieldNameOverridesType.DESC_FIELD_NAME_OVERRIDE,
                                                       parentDescValue));
        assertThat(parentRecord, hasChildRecordThat(hasCreatedFieldRecord(AuditedChild1WithFieldNameOverridesType.DESC_FIELD_NAME_OVERRIDE,
                                                                          child1DescValue)));
        assertThat(parentRecord, hasChildRecordThat(hasCreatedFieldRecord(AuditedChild2WithFieldNameOverridesType.DESC_FIELD_NAME_OVERRIDE,
                                                                          child2DescValue)));
    }

    @Test
    public void overrideFieldNamesInAncestor() {
        final String nameValue = "nameValue";
        final String ancestorNameValue = "ancestorNameValue";
        final String ancestorDescValue = "ancestorDescValue";

        mainWithAncestorPL.create(singletonList(entityWithAncestorCreateCmd()
                                                    .with(AuditedWithAncestorFieldNameOverridesType.ANCESTOR_ID, ANCESTOR_ID)
                                                    .with(AuditedWithAncestorFieldNameOverridesType.NAME, nameValue)),
                                  mainWithAncestorFlowConfig);


        final List<? extends AuditRecord> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        assertThat(auditRecords.get(0), hasMandatoryFieldValue(NAME_FIELD_NAME_OVERRIDE,
                                                               ancestorNameValue));
        assertThat(auditRecords.get(0), hasMandatoryFieldValue(DESC_FIELD_NAME_OVERRIDE,
                                                               ancestorDescValue));
    }

    private <E extends EntityType<E>> PersistenceLayer<E> persistenceLayer() {
        return new PersistenceLayer<>(plContext);
    }

    private <E extends EntityType<E>> ChangeFlowConfig.Builder<E> flowConfigBuilder(final E entityType) {
        return ChangeFlowConfigBuilderFactory.newInstance(plContext, entityType);
    }

    private CreateAuditedWithFieldNameOverridesCommand parentCreateCmd() {
        return new CreateAuditedWithFieldNameOverridesCommand();
    }

    private CreateAuditedChild1WithFieldNameOverridesCommand child1CreateCmd() {
        return new CreateAuditedChild1WithFieldNameOverridesCommand();
    }

    private CreateAuditedChild2WithFieldNameOverridesCommand child2CreateCmd() {
        return new CreateAuditedChild2WithFieldNameOverridesCommand();
    }

    private CreateAuditedWithAncestorFieldNameOverridesCommand entityWithAncestorCreateCmd() {
        return new CreateAuditedWithAncestorFieldNameOverridesCommand();
    }
}
