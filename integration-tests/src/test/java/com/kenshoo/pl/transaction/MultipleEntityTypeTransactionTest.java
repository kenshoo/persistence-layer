package com.kenshoo.pl.transaction;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.jooq.DataTableUtils;
import com.kenshoo.jooq.TestJooqConfig;
import com.kenshoo.pl.TypedFluidPersistenceCmdBuilder;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.one2many.relatedByPK.IdGenerator;
import com.kenshoo.pl.one2many.relatedByPK.IntegerIdGeneratorEnricher;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.impl.DSL;
import org.jooq.lambda.Seq;
import org.junit.*;
import static org.hamcrest.CoreMatchers.is;

import java.util.List;

import static com.kenshoo.pl.TypedFluidPersistenceCmdBuilder.fluid;
import static org.hamcrest.MatcherAssert.assertThat;

public class MultipleEntityTypeTransactionTest {

    private static final TablesSetup tablesSetup = new TablesSetup();

    private static final Table1 TABLE_1 = Table1.INSTANCE;
    private static final Table2 TABLE_2 = Table2.INSTANCE;
    private static final List<DataTable> ALL_TABLES = List.of(TABLE_1, TABLE_2);

    private final IdGenerator idGenerator = new IdGenerator();

    private final DSLContext jooq = TestJooqConfig.create();

    private PLContext plContext;

    private PersistenceLayer<Entity1> persistenceLayer1;
    private PersistenceLayer<Entity2> persistenceLayer2;

    @Before
    public void setupTables() {
        persistenceLayer1 = new PersistenceLayer<>(jooq);
        persistenceLayer2 = new PersistenceLayer<>(jooq);
        plContext = new PLContext.Builder(jooq).build();

        if (tablesSetup.alreadyCreated) {
            return;
        }
        tablesSetup.alreadyCreated = true;
        tablesSetup.staticDSLContext = jooq;

        ALL_TABLES.forEach(table -> DataTableUtils.createTable(jooq, table));
        jooq.alterTable(TABLE_1).add(DSL.constraint("unique_id_1").unique(TABLE_1.id)).execute();
        jooq.alterTable(TABLE_2).add(DSL.constraint("unique_id_2").unique(TABLE_2.id)).execute();
    }

    @Ignore
    @Test
    public void commitTwoUnrelatedTypesInTransaction() {

        var results = jooq.transactionResult(configuration -> {
            final var result1 = insertEntity1(newEntity1().with(Entity1.NAME, "name1"));
            final var result2 = insertEntity2(newEntity2().with(Entity2.NAME, "name2"));
            return List.of(result1.getChangeResults().iterator().next().getCommand().get(Entity1.ID),
                           result2.getChangeResults().iterator().next().getCommand().get(Entity2.ID));
        });

        List<CurrentEntityState> fetch1 = plContext.select(Entity1.NAME).from(Entity1.INSTANCE).where(Entity1.ID.eq(results.get(0))).fetch();
        List<CurrentEntityState> fetch2 = plContext.select(Entity2.NAME).from(Entity2.INSTANCE).where(Entity2.ID.eq(results.get(1))).fetch();
        assertThat(fetch1.get(0).get(Entity1.NAME), is("name1"));
        assertThat(fetch2.get(0).get(Entity2.NAME), is("name2"));
    }

    @Ignore
    @Test
    public void rollbackTwoUnrelatedTypesInTransaction() {

        var results = jooq.transactionResult(configuration -> {
            final var result1 = insertEntity1(newEntity1().with(Entity1.NAME, "name1"));
            final var result2 = insertEntity2(newEntity2().with(Entity2.NAME, "name2"));
            return List.of(result1.getChangeResults().iterator().next().getCommand().get(Entity1.ID),
                    result2.getChangeResults().iterator().next().getCommand().get(Entity2.ID));
        });

        try {
            jooq.transaction(configuration -> {
                updateEntity1(updateEntity1(IdentifierType.uniqueKey(Entity1.ID).createIdentifier(results.get(0))).with(Entity1.NAME, "name1_update"));
                updateEntity2(updateEntity2(IdentifierType.uniqueKey(Entity2.ID).createIdentifier(results.get(1))).with(Entity2.NAME, "name2_update"));
                throw new RuntimeException("Rollback transaction");
            });
        } catch (Exception e){
        }


        List<CurrentEntityState> fetch1 = plContext.select(Entity1.NAME).from(Entity1.INSTANCE).where(Entity1.ID.eq(results.get(0))).fetch();
        List<CurrentEntityState> fetch2 = plContext.select(Entity2.NAME).from(Entity2.INSTANCE).where(Entity2.ID.eq(results.get(1))).fetch();
        assertThat(fetch1.get(0).get(Entity1.NAME), is("name1"));
        assertThat(fetch2.get(0).get(Entity2.NAME), is("name2"));
    }


    @After
    public void clearTables() {
        ALL_TABLES.stream()
                  .map(jooq::deleteFrom)
                  .forEach(Query::execute);
    }

    @AfterClass
    public static void dropTables() {
        ALL_TABLES.stream()
                  .map(tablesSetup.staticDSLContext::dropTableIfExists)
                  .forEach(Query::execute);
    }

    @SafeVarargs
    private CreateResult<Entity1, Identifier<Entity1>> insertEntity1(TypedFluidPersistenceCmdBuilder<Entity1, CreateEntityCommand<Entity1>>... cmds) {
        return insert(persistenceLayer1, Entity1.INSTANCE, Entity1.ID, cmds);
    }

    @SafeVarargs
    private CreateResult<Entity2, Identifier<Entity2>> insertEntity2(TypedFluidPersistenceCmdBuilder<Entity2, CreateEntityCommand<Entity2>>... cmds) {
        return insert(persistenceLayer2, Entity2.INSTANCE, Entity2.ID, cmds);
    }

    @SafeVarargs
    private <E extends EntityType<E>> CreateResult<E, Identifier<E>> insert(PersistenceLayer<E> persistenceLayer,
                                                                            E entityType,
                                                                            EntityField<E, Integer> idField,
                                                                            TypedFluidPersistenceCmdBuilder<E, CreateEntityCommand<E>>... cmds) {
        return insert(persistenceLayer, flow(entityType, idField), cmds);
    }

    @SafeVarargs
    private <E extends EntityType<E>> CreateResult<E, Identifier<E>> insert(PersistenceLayer<E> persistenceLayer,
                                                                            ChangeFlowConfig.Builder<E> flow,
                                                                            TypedFluidPersistenceCmdBuilder<E, CreateEntityCommand<E>>... cmdBuilders) {
        final List<CreateEntityCommand<E>> createCmds = Seq.of(cmdBuilders)
                                                           .map(TypedFluidPersistenceCmdBuilder::get)
                                                           .toList();

        return persistenceLayer.create(createCmds, flow.build());
    }

    @SafeVarargs
    private UpdateResult<Entity1, Identifier<Entity1>> updateEntity1(TypedFluidPersistenceCmdBuilder<Entity1, UpdateEntityCommand<Entity1, Identifier<Entity1>>>... cmds) {
        return update(persistenceLayer1, Entity1.INSTANCE, Entity1.ID, cmds);
    }

    @SafeVarargs
    private UpdateResult<Entity2, Identifier<Entity2>> updateEntity2(TypedFluidPersistenceCmdBuilder<Entity2, UpdateEntityCommand<Entity2, Identifier<Entity2>>>... cmds) {
        return update(persistenceLayer2, Entity2.INSTANCE, Entity2.ID, cmds);
    }

    @SafeVarargs
    private <E extends EntityType<E>> UpdateResult<E, Identifier<E>> update(PersistenceLayer<E> persistenceLayer,
                                                                            E entityType,
                                                                            EntityField<E, Integer> idField,
                                                                            TypedFluidPersistenceCmdBuilder<E, UpdateEntityCommand<E, Identifier<E>>>... cmds) {
        return update(persistenceLayer, flow(entityType, idField), cmds);
    }

    @SafeVarargs
    private <ID extends Identifier<E>, E extends EntityType<E>> UpdateResult<E, ID> update(PersistenceLayer<E> persistenceLayer,
                                                                            ChangeFlowConfig.Builder<E> flow,
                                                                            TypedFluidPersistenceCmdBuilder<E, UpdateEntityCommand<E, ID>>... cmdBuilders) {
        final List<UpdateEntityCommand<E, ID>> createCmds = Seq.of(cmdBuilders)
                .map(TypedFluidPersistenceCmdBuilder::get)
                .toList();

        return persistenceLayer.update(createCmds, flow.build());
    }

    private <E extends EntityType<E>> ChangeFlowConfig.Builder<E> flow(E entityType, EntityField<E, Integer> idField) {
        final IntegerIdGeneratorEnricher<E> idEnricher = new IntegerIdGeneratorEnricher<>(idGenerator, idField);
        return ChangeFlowConfigBuilderFactory.newInstance(plContext, entityType)
                                             .withPostFetchCommandEnricher(idEnricher);
    }

    private TypedFluidPersistenceCmdBuilder<Entity1, CreateEntityCommand<Entity1>> newEntity1() {
        return newEntity(Entity1.INSTANCE);
    }

    private TypedFluidPersistenceCmdBuilder<Entity2, CreateEntityCommand<Entity2>> newEntity2() {
        return newEntity(Entity2.INSTANCE);
    }

    private  <ID extends Identifier<Entity1>>TypedFluidPersistenceCmdBuilder<Entity1, UpdateEntityCommand<Entity1, ID>> updateEntity1(ID key) {
        return updateEntity(Entity1.INSTANCE, key);
    }

    private  <ID extends Identifier<Entity2>>TypedFluidPersistenceCmdBuilder<Entity2, UpdateEntityCommand<Entity2, ID>> updateEntity2(ID key) {
        return updateEntity(Entity2.INSTANCE, key);
    }

    private <E extends EntityType<E>> TypedFluidPersistenceCmdBuilder<E, CreateEntityCommand<E>> newEntity(E entityType) {
        return fluid(new CreateEntityCommand<>(entityType));
    }

    private <E extends EntityType<E>, ID extends Identifier<E>> TypedFluidPersistenceCmdBuilder<E, UpdateEntityCommand<E, ID>> updateEntity(E entityType, ID key) {
        return fluid(new UpdateEntityCommand<>(entityType, key));
    }


    private static class TablesSetup {
        DSLContext staticDSLContext;
        boolean alreadyCreated = false;
    }
}
