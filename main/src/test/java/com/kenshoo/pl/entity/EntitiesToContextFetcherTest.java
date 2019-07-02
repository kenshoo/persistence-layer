package com.kenshoo.pl.entity;

import com.kenshoo.pl.FluidPersistenceCmdBuilder;
import com.kenshoo.pl.entity.internal.EntitiesFetcher;
import com.kenshoo.pl.entity.internal.EntitiesToContextFetcher;
import org.jooq.lambda.Seq;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.kenshoo.pl.FluidPersistenceCmdBuilder.fluid;
import static com.kenshoo.pl.entity.ChangeOperation.CREATE;
import static com.kenshoo.pl.entity.ChangeOperation.UPDATE;
import static com.kenshoo.pl.entity.TestChildEntity.PARENT_ID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;


@RunWith(MockitoJUnitRunner.class)
public class EntitiesToContextFetcherTest {

    private final static Entity MISSING = Entity.EMPTY;

    @Mock
    private EntitiesFetcher fetcher;

    @Mock
    private PLContext plContext;

    private EntitiesToContextFetcher classUnderTest;

    @Before
    public void setup() {
        classUnderTest = new EntitiesToContextFetcher(fetcher);
    }

    @Test
    public void CREATE_entity_with_missing_parent_by_required_relation_returns_MISSING() {
        ChangeEntityCommand<TestChildEntity> cmd = createChild().with(PARENT_ID, -1) .get();
        ChangeContext context = fetchToContext(childFlow(), CREATE, cmd);
        assertThat(context.getEntity(cmd), is(MISSING));
    }

    @Test
    public void CREATE_entity_without_fk_returns_not_missing() {
        ChangeEntityCommand<TestEntity> cmd = createParent().get();
        ChangeContext context = fetchToContext(parentFlow(), CREATE, cmd);
        assertThat(context.getEntity(cmd), not(MISSING));
    }

    @Test
    public void UPDATE_entity_not_in_DB_returns_MISSING() {
        ChangeEntityCommand<TestEntity> cmd = updateParent(1).get();
        ChangeContext context = fetchToContext(parentFlow(), UPDATE, cmd);
        assertThat(context.getEntity(cmd), is(MISSING));
    }

    @SafeVarargs
    private final <E extends EntityType<E>> ChangeContext fetchToContext(ChangeFlowConfig.Builder flow, ChangeOperation op, ChangeEntityCommand<E>... commands) {
        ChangeContext ctx = new ChangeContext();
        classUnderTest.fetchEntities(Seq.of(commands).toList(), op, ctx, flow.build());
        return ctx;
    }

    private FluidPersistenceCmdBuilder<TestEntity> createParent() {
        return fluid(new CreateEntityCommand<>(TestEntity.INSTANCE));
    }

    private FluidPersistenceCmdBuilder<TestEntity> updateParent(int id) {
        return fluid(new UpdateEntityCommand<>(TestEntity.INSTANCE, new TestEntity.Key(id)));
    }

    private FluidPersistenceCmdBuilder<TestChildEntity> createChild() {
        return fluid(new CreateEntityCommand<>(TestChildEntity.INSTANCE));
    }

    private FluidPersistenceCmdBuilder<TestChildEntity> updateChild(int ordinal) {
        return fluid(new UpdateEntityCommand<>(TestChildEntity.INSTANCE, new TestChildEntity.Ordinal(ordinal)));
    }

    private ChangeFlowConfig.Builder parentFlow() {
        return ChangeFlowConfigBuilderFactory.newInstance(plContext, TestEntity.INSTANCE);
    }

    private ChangeFlowConfig.Builder childFlow() {
        return ChangeFlowConfigBuilderFactory.newInstance(plContext, TestChildEntity.INSTANCE);
    }
}
