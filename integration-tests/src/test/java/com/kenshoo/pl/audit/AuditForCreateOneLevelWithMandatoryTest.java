package com.kenshoo.pl.audit;

import com.google.common.collect.ImmutableList;
import com.kenshoo.jooq.DataTable;
import com.kenshoo.jooq.DataTableUtils;
import com.kenshoo.jooq.TestJooqConfig;
import com.kenshoo.pl.audit.commands.CreateAuditedWithAncestorMandatoryCommand;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.audit.AuditRecord;
import com.kenshoo.pl.entity.internal.audit.AncestorTable;
import com.kenshoo.pl.entity.internal.audit.MainWithAncestorTable;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithAncestorMandatoryType;
import com.kenshoo.pl.entity.internal.audit.entitytypes.NotAuditedAncestorType;
import org.jooq.DSLContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Objects;

import static com.kenshoo.pl.entity.ChangeOperation.CREATE;
import static com.kenshoo.pl.entity.matchers.audit.AuditRecordMatchers.*;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class AuditForCreateOneLevelWithMandatoryTest {

    private static final String NAME = "name";
    private static final String DESC = "desc";
    private static final long ANCESTOR_ID = 11L;
    private static final String ANCESTOR_NAME = "ancestorName";
    private static final String ANCESTOR_DESC = "ancestorDesc";

    private static final List<DataTable> ALL_TABLES = ImmutableList.of(MainWithAncestorTable.INSTANCE,
                                                                       AncestorTable.INSTANCE);

    private PLContext plContext;
    private InMemoryAuditRecordPublisher auditRecordPublisher;

    private ChangeFlowConfig<AuditedWithAncestorMandatoryType> auditedWithAncestorMandatoryConfig;

    private PersistenceLayer<AuditedWithAncestorMandatoryType> auditedWithAncestorMandatoryPL;

    @Before
    public void setUp() {
        final DSLContext dslContext = TestJooqConfig.create();
        auditRecordPublisher = new InMemoryAuditRecordPublisher();
        plContext = new PLContext.Builder(dslContext)
            .withFeaturePredicate(__ -> true)
            .withAuditRecordPublisher(auditRecordPublisher)
            .build();

        auditedWithAncestorMandatoryConfig = flowConfig(AuditedWithAncestorMandatoryType.INSTANCE);

        auditedWithAncestorMandatoryPL = persistenceLayer();

        ALL_TABLES.forEach(table -> DataTableUtils.createTable(dslContext, table));

        dslContext.insertInto(AncestorTable.INSTANCE)
                  .set(AncestorTable.INSTANCE.id, ANCESTOR_ID)
                  .set(AncestorTable.INSTANCE.name, ANCESTOR_NAME)
                  .set(AncestorTable.INSTANCE.desc, ANCESTOR_DESC)
                  .execute();
    }

    @After
    public void tearDown() {
        ALL_TABLES.forEach(table -> plContext.dslContext().dropTable(table).execute());
    }

    @Test
    public void oneEntity_WithEntityLevelMandatoryFields_AndFieldsInCmd_ShouldCreateMandatoryFieldsAndFieldRecords() {
        final CreateResult<AuditedWithAncestorMandatoryType, Identifier<AuditedWithAncestorMandatoryType>> createResult =
            auditedWithAncestorMandatoryPL.create(singletonList(new CreateAuditedWithAncestorMandatoryCommand()
                                                                    .with(AuditedWithAncestorMandatoryType.ANCESTOR_ID, ANCESTOR_ID)
                                                                    .with(AuditedWithAncestorMandatoryType.NAME, NAME)
                                                                    .with(AuditedWithAncestorMandatoryType.DESC, DESC)),
                                                  auditedWithAncestorMandatoryConfig);
        final long id = extractIdFromResult(createResult, AuditedWithAncestorMandatoryType.ID);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<AuditedWithAncestorMandatoryType> auditRecord = typed(auditRecords.get(0));
        //noinspection unchecked
        assertThat(auditRecord, allOf(hasEntityType(AuditedWithAncestorMandatoryType.INSTANCE),
                                      hasEntityId(String.valueOf(id)),
                                      hasMandatoryFieldValue(NotAuditedAncestorType.NAME, ANCESTOR_NAME),
                                      hasMandatoryFieldValue(NotAuditedAncestorType.DESC, ANCESTOR_DESC),
                                      hasOperator(CREATE),
                                      hasCreatedFieldRecord(AuditedWithAncestorMandatoryType.NAME, NAME),
                                      hasCreatedFieldRecord(AuditedWithAncestorMandatoryType.DESC, DESC)));
    }

    @Test
    public void oneEntity_WithEntityLevelMandatoryFields_AndNoFieldsInCmd_ShouldCreateMandatoryFieldsOnly() {
        final CreateResult<AuditedWithAncestorMandatoryType, Identifier<AuditedWithAncestorMandatoryType>> createResult =
            auditedWithAncestorMandatoryPL.create(singletonList(new CreateAuditedWithAncestorMandatoryCommand()
                                                                    .with(AuditedWithAncestorMandatoryType.ANCESTOR_ID, ANCESTOR_ID)),
                                                  auditedWithAncestorMandatoryConfig);
        final long id = extractIdFromResult(createResult, AuditedWithAncestorMandatoryType.ID);

        final List<? extends AuditRecord<?>> auditRecords = auditRecordPublisher.getAuditRecords().collect(toList());

        assertThat("Incorrect number of published records",
                   auditRecords, hasSize(1));
        final AuditRecord<AuditedWithAncestorMandatoryType> auditRecord = typed(auditRecords.get(0));
        assertThat(auditRecord, allOf(hasEntityType(AuditedWithAncestorMandatoryType.INSTANCE),
                                      hasEntityId(String.valueOf(id)),
                                      hasMandatoryFieldValue(NotAuditedAncestorType.NAME, ANCESTOR_NAME),
                                      hasMandatoryFieldValue(NotAuditedAncestorType.DESC, ANCESTOR_DESC),
                                      hasOperator(CREATE),
                                      hasNoFieldRecords()));
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

    @SuppressWarnings("unchecked")
    private <E extends EntityType<E>> AuditRecord<E> typed(final AuditRecord<?> auditRecord) {
        return (AuditRecord<E>) auditRecord;
    }
}
