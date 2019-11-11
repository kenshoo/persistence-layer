package com.kenshoo.pl.entity;

import com.google.common.collect.ImmutableList;
import com.kenshoo.jooq.AbstractDataTable;
import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.FluidPersistenceCmdBuilder;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.spi.FieldValueSupplier;
import com.kenshoo.pl.entity.spi.NotSuppliedException;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Stream;

import static com.kenshoo.pl.FluidPersistenceCmdBuilder.fluid;
import static com.kenshoo.pl.entity.TestChildEntity.CHILD_FIELD_1;
import static com.kenshoo.pl.entity.TestGrandChildEntity.GRAND_CHILD_FIELD_1;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;

@RunWith(MockitoJUnitRunner.Silent.class)
public class FieldsToFetchBuilderTest {

    private ChangeFlowConfig<TestEntity> flowConfig;

    @Mock
    private PLContext plContext;
    @InjectMocks
    private FieldsToFetchBuilder<TestEntity> fieldsToFetchBuilder;

    @Before
    public void init() {
        flowConfig = ChangeFlowConfigBuilderFactory.newInstance(plContext, TestEntity.INSTANCE).
                            withChildFlowBuilder(ChangeFlowConfigBuilderFactory.newInstance(plContext, TestChildEntity.INSTANCE).
                                withChildFlowBuilder(ChangeFlowConfigBuilderFactory.newInstance(plContext, TestGrandChildEntity.INSTANCE))).
                                    build();
    }

    @Test
    public void no_requested_entity_field_for_update() {
        Collection<FieldFetchRequest> requests = calculateRequiredFields(updateParent());

        assertThat(requests, containsInAnyOrder(
                requested(TestEntity.ID).queryOn(TestEntity.INSTANCE).askedBy(TestEntity.INSTANCE).build()
        ));

    }

    @Test
    public void no_requested_entity_field_for_create() {
        assertThat(calculateRequiredFields(createParent()), hasSize(0));
    }

    @Test
    public void one_entity_field_requested_by_entity_for_update() {

        Collection<FieldFetchRequest> requests = calculateRequiredFields(
                updateParent().with(TestEntity.FIELD_1, supplierRequiring(TestEntity.FIELD_1))
        );

        assertThat(requests, containsInAnyOrder(
                requested(TestEntity.ID).queryOn(TestEntity.INSTANCE).askedBy(TestEntity.INSTANCE).build(),
                requested(TestEntity.FIELD_1).queryOn(TestEntity.INSTANCE).askedBy(TestEntity.INSTANCE).build()
        ));
    }


    @Test
    public void one_entity_field_requested_by_entity_for_upsert() {

        Collection<FieldFetchRequest> requests = calculateRequiredFields(
                upsertParent().with(TestEntity.FIELD_1, supplierRequiring(TestParentEntity.SOME_FIELD))
        );

        assertThat(requests, containsInAnyOrder(
                requested(TestEntity.ID).queryOn(TestEntity.INSTANCE).askedBy(TestEntity.INSTANCE).build(),
                requested(TestEntity.FIELD_1).queryOn(TestEntity.INSTANCE).askedBy(TestEntity.INSTANCE).build(),
                requested(TestParentEntity.SOME_FIELD).queryOn(TestEntity.INSTANCE).askedBy(TestEntity.INSTANCE).build()
        ));
    }

    @Test
    public void one_external_field_requested_by_entity_for_update() {

        Collection<FieldFetchRequest> requests = calculateRequiredFields(
                updateParent().with(TestEntity.FIELD_1, supplierRequiring(TestParentEntity.SOME_FIELD))
        );

        assertThat(requests, containsInAnyOrder(
                requested(TestEntity.ID).queryOn(TestEntity.INSTANCE).askedBy(TestEntity.INSTANCE).build(),
                requested(TestEntity.FIELD_1).queryOn(TestEntity.INSTANCE).askedBy(TestEntity.INSTANCE).build(),
                requested(TestParentEntity.SOME_FIELD).queryOn(TestEntity.INSTANCE).askedBy(TestEntity.INSTANCE).build()
        ));
    }

    @Test
    public void one_external_field_requested_by_entity_for_create() {

        Collection<FieldFetchRequest> requests = calculateRequiredFields(
                createParent().with(TestEntity.FIELD_1, supplierRequiring(TestParentEntity.SOME_FIELD))
        );

        assertThat(requests, containsInAnyOrder(
                requested(TestParentEntity.SOME_FIELD).queryOn(TestEntity.INSTANCE).askedBy(TestEntity.INSTANCE).build()
        ));
    }


    @Test
    public void one_child_entity_field_requested_by_child_entity_for_update() {

        Collection<FieldFetchRequest> requests = calculateRequiredFields(
                updateParent()
                        .withChild(
                                updateChild().with(CHILD_FIELD_1, supplierRequiring(CHILD_FIELD_1))
                        )
                );

        assertThat(requests, containsInAnyOrder(
                requested(TestEntity.ID).queryOn(TestEntity.INSTANCE).askedBy(TestEntity.INSTANCE).build(),
                requested(TestChildEntity.ORDINAL).queryOn(TestChildEntity.INSTANCE).askedBy(TestChildEntity.INSTANCE).build(),
                requested(CHILD_FIELD_1).queryOn(TestChildEntity.INSTANCE).askedBy(TestChildEntity.INSTANCE).build()
        ));
    }

    @Test
    public void one_entity_field_requested_by_child_entity_for_update() {

        Collection<FieldFetchRequest> requests = calculateRequiredFields(
                updateParent()
                        .withChild(
                                updateChild().with(CHILD_FIELD_1, supplierRequiring(TestEntity.FIELD_1))
                        )
        );

        assertThat(requests, containsInAnyOrder(
                requested(TestEntity.ID).queryOn(TestEntity.INSTANCE).askedBy(TestEntity.INSTANCE).build(),
                requested(TestChildEntity.ORDINAL).queryOn(TestChildEntity.INSTANCE).askedBy(TestChildEntity.INSTANCE).build(),
                requested(CHILD_FIELD_1).queryOn(TestChildEntity.INSTANCE).askedBy(TestChildEntity.INSTANCE).build(),
                requested(TestEntity.FIELD_1).queryOn(TestEntity.INSTANCE).askedBy(TestChildEntity.INSTANCE).build()
        ));
    }

    @Test
    public void when_updated_child_requires_external_field_then_query_for_root_for_both_CREATE_and_UPDATE() {

        Collection<FieldFetchRequest> requests = calculateRequiredFields(
                updateParent()
                        .withChild(
                                updateChild().with(CHILD_FIELD_1, supplierRequiring(TestParentEntity.SOME_FIELD))
                        )
        );

        assertThat(requests, containsInAnyOrder(
                requested(TestEntity.ID).queryOn(TestEntity.INSTANCE).askedBy(TestEntity.INSTANCE).build(),
                requested(TestChildEntity.ORDINAL).queryOn(TestChildEntity.INSTANCE).askedBy(TestChildEntity.INSTANCE).build(),
                requested(TestChildEntity.CHILD_FIELD_1).queryOn(TestChildEntity.INSTANCE).askedBy(TestChildEntity.INSTANCE).build(),
                requested(TestParentEntity.SOME_FIELD).queryOn(TestEntity.INSTANCE).askedBy(TestChildEntity.INSTANCE).build()
        ));
    }

    @Test
    public void when_created_child_requires_external_field_then_query_for_root_for_both_CREATE_and_UPDATE() {

        Collection<FieldFetchRequest> requests = calculateRequiredFields(
                createParent()
                        .withChild(
                                createChild().with(CHILD_FIELD_1, supplierRequiring(TestParentEntity.SOME_FIELD))
                        )
        );

        assertThat(requests, containsInAnyOrder(
                requested(TestParentEntity.SOME_FIELD).queryOn(TestEntity.INSTANCE).askedBy(TestChildEntity.INSTANCE).build()
        ));
    }


    @Test
    public void when_updated_3rd_requesting_from_2nd_then_query_on_2nd_for_update() {

        Collection<FieldFetchRequest> requests = calculateRequiredFields(
                updateParent()
                        .withChild(
                            updateChild()
                                    .withChild(
                                            updateGrandChild().with(GRAND_CHILD_FIELD_1, supplierRequiring(CHILD_FIELD_1))
                                    )
                        )
        );

        assertThat(requests, containsInAnyOrder(
                requested(TestEntity.ID).queryOn(TestEntity.INSTANCE).askedBy(TestEntity.INSTANCE).build(),
                requested(TestChildEntity.ORDINAL).queryOn(TestChildEntity.INSTANCE).askedBy(TestChildEntity.INSTANCE).build(),
                requested(TestGrandChildEntity.ID).queryOn(TestGrandChildEntity.INSTANCE).askedBy(TestGrandChildEntity.INSTANCE).build(),
                requested(CHILD_FIELD_1).queryOn(TestChildEntity.INSTANCE).askedBy(TestGrandChildEntity.INSTANCE).build(),
                requested(TestGrandChildEntity.GRAND_CHILD_FIELD_1).queryOn(TestGrandChildEntity.INSTANCE).askedBy(TestGrandChildEntity.INSTANCE).build()
        ));
    }

    @Test
    public void one_child_entity_field_requested_by_grand_child_entity_for_create() {

        Collection<FieldFetchRequest> requests = calculateRequiredFields(
                createParent()
                        .withChild(
                                createChild()
                                        .withChild(
                                                createGrandChild().with(GRAND_CHILD_FIELD_1, supplierRequiring(CHILD_FIELD_1))
                                        )
                        )
        );

        assertThat(requests, containsInAnyOrder(
                //
                // why UPDATE if grand child is CREATE ?????????????????????????????????????????????????
                //
                requested(CHILD_FIELD_1).queryOn(TestChildEntity.INSTANCE).askedBy(TestGrandChildEntity.INSTANCE).build()
        ));
    }

    private FluidPersistenceCmdBuilder<TestEntity> updateParent() {
        return fluid(new UpdateEntityCommand<>(TestEntity.INSTANCE, new TestEntity.Key(1)));
    }

    private FluidPersistenceCmdBuilder<TestEntity> createParent() {
        return fluid(new CreateEntityCommand<>(TestEntity.INSTANCE));
    }

    private FluidPersistenceCmdBuilder<TestEntity> upsertParent() {
        return fluid(new InsertOnDuplicateUpdateCommand<>(TestEntity.INSTANCE, new TestEntity.Key(1)));
    }

    private FluidPersistenceCmdBuilder<TestChildEntity> updateChild() {
        return fluid(new UpdateEntityCommand<>(TestChildEntity.INSTANCE, new TestChildEntity.Ordinal(1)));
    }

    private FluidPersistenceCmdBuilder<TestChildEntity> createChild() {
        return fluid(new CreateEntityCommand<>(TestChildEntity.INSTANCE));
    }

    private FluidPersistenceCmdBuilder<TestGrandChildEntity> updateGrandChild() {
        return fluid(new UpdateEntityCommand<>(TestGrandChildEntity.INSTANCE, new TestGrandChildEntity.Key(1)));
    }

    private FluidPersistenceCmdBuilder<TestGrandChildEntity> createGrandChild() {
        return fluid(new CreateEntityCommand<>(TestGrandChildEntity.INSTANCE));
    }

    private <T> FieldValueSupplier<T> supplierRequiring(final EntityField<?, ?> requestedField) {
        return new FieldValueSupplier<T>() {
            @Override
            public T supply(Entity entity) throws NotSuppliedException {
                return null;
            }
            public Stream<EntityField<?, ?>> fetchFields(ChangeOperation changeOperation) {
                return Stream.of(requestedField);
            }
        };
    }

    private FieldFetchRequest.Builder requested(EntityField<?, ?> field) {
        return new FieldFetchRequest.Builder().field(field);
    }

    private Collection<FieldFetchRequest> calculateRequiredFields(FluidPersistenceCmdBuilder<TestEntity> cmd) {
        return new HashSet<>(fieldsToFetchBuilder.build(ImmutableList.of(cmd.get()), flowConfig));
    }

    static public class TestParentEntity extends AbstractEntityType<TestParentEntity> {

        public static final TestParentEntity INSTANCE = new TestParentEntity();

        private TestParentEntity() {
            super("parent");
        }

        @Id
        static final EntityField<TestParentEntity, Integer> ID = INSTANCE.field(TestParentTable.TABLE.id);
        static final EntityField<TestParentEntity, String> SOME_FIELD = INSTANCE.field(TestParentTable.TABLE.some_field);

        @Override
        public DataTable getPrimaryTable() {
            return TestParentTable.TABLE;
        }

    }

    static public class TestParentTable extends AbstractDataTable<TestParentTable> {

        public static final TestParentTable TABLE = new TestParentTable();

        private TestParentTable() {
            super("parent");
        }

        final TableField<Record, Integer> id = createPKField("id", SQLDataType.INTEGER);
        final TableField<Record, String> some_field = createField("field_1", SQLDataType.VARCHAR.length(10));

        @Override
        public TestParentTable as(String alias) {
            return null;
        }
    }

}