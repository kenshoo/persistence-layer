package com.kenshoo.pl.entity;

import com.google.common.collect.ImmutableList;
import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.FluidPersistenceCmdBuilder;
import com.kenshoo.pl.entity.spi.FieldValueSupplier;
import com.kenshoo.pl.entity.spi.NotSuppliedException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.stream.Stream;

import static com.kenshoo.pl.FluidPersistenceCmdBuilder.fluid;
import static com.kenshoo.pl.entity.TestChildEntity.CHILD_FIELD_1;
import static com.kenshoo.pl.entity.TestGrandChildEntity.GRAND_CHILD_FIELD_1;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class FieldsToFetchBuilderTest {
    @Mock
    private ChangeFlowConfig<TestEntity> flowConfig;
    @Mock
    private ChangeFlowConfig<TestChildEntity> childFlowConfig;
    @Mock
    private ChangeFlowConfig<TestGrandChildEntity> grandChildFlowConfig;
    @Mock
    private EntityField<?, ?> extField;
    @Mock
    private AbstractEntityType entityType;
    @Mock
    private EntityFieldDbAdapter adapter;
    @Mock
    private DataTable primaryTable;

    @InjectMocks
    private FieldsToFetchBuilder<TestEntity> fieldsToFetchBuilder;

    @Before
    public void init() {
        when(flowConfig.getEntityType()).thenReturn(TestEntity.INSTANCE);
        when(childFlowConfig.getEntityType()).thenReturn(TestChildEntity.INSTANCE);
        when(grandChildFlowConfig.getEntityType()).thenReturn(TestGrandChildEntity.INSTANCE);
        when(flowConfig.childFlows()).thenReturn(ImmutableList.of(childFlowConfig));
        when(childFlowConfig.childFlows()).thenReturn(ImmutableList.of(grandChildFlowConfig));
        when(extField.getEntityType()).thenReturn(entityType);
        when(extField.getDbAdapter()).thenReturn(adapter);
        when(adapter.getTable()).thenReturn(primaryTable);
    }

    @Test
    public void no_requested_entity_field_for_update() {
        assertThat(calculateRequiredFields(updateParent()), hasSize(0));
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
                requested(TestEntity.FIELD_1).queryOn(TestEntity.INSTANCE).askedBy(TestEntity.INSTANCE).build()
        ));
    }


    @Test
    public void one_entity_field_requested_by_entity_for_upsert() {

        Collection<FieldFetchRequest> requests = calculateRequiredFields(
                upsertParent().with(TestEntity.FIELD_1, supplierRequiring(extField))
        );

        assertThat(requests, containsInAnyOrder(
                requested(extField).queryOn(TestEntity.INSTANCE).askedBy(TestEntity.INSTANCE).build(),
                requested(extField).queryOn(TestEntity.INSTANCE).askedBy(TestEntity.INSTANCE).build()
        ));
    }

    @Test
    public void one_external_field_requested_by_entity_for_update() {

        Collection<FieldFetchRequest> requests = calculateRequiredFields(
                updateParent().with(TestEntity.FIELD_1, supplierRequiring(extField))
        );

        assertThat(requests, containsInAnyOrder(
                requested(extField).queryOn(TestEntity.INSTANCE).askedBy(TestEntity.INSTANCE).build()
        ));
    }

    @Test
    public void one_external_field_requested_by_entity_for_create() {

        Collection<FieldFetchRequest> requests = calculateRequiredFields(
                createParent().with(TestEntity.FIELD_1, supplierRequiring(extField))
        );

        assertThat(requests, containsInAnyOrder(
                requested(extField).queryOn(TestEntity.INSTANCE).askedBy(TestEntity.INSTANCE).build()
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
                requested(TestEntity.FIELD_1).queryOn(TestEntity.INSTANCE).askedBy(TestChildEntity.INSTANCE).build()
        ));
    }

    @Test
    public void when_updated_child_requires_external_field_then_query_for_root_for_both_CREATE_and_UPDATE() {

        Collection<FieldFetchRequest> requests = calculateRequiredFields(
                updateParent()
                        .withChild(
                                updateChild().with(CHILD_FIELD_1, supplierRequiring(extField))
                        )
        );

        assertThat(requests, containsInAnyOrder(
                requested(extField).queryOn(TestEntity.INSTANCE).askedBy(TestChildEntity.INSTANCE).build()
        ));
    }

    @Test
    public void when_created_child_requires_external_field_then_query_for_root_for_both_CREATE_and_UPDATE() {

        Collection<FieldFetchRequest> requests = calculateRequiredFields(
                createParent()
                        .withChild(
                                createChild().with(CHILD_FIELD_1, supplierRequiring(extField))
                        )
        );

        assertThat(requests, containsInAnyOrder(
                requested(extField).queryOn(TestEntity.INSTANCE).askedBy(TestChildEntity.INSTANCE).build()
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
                requested(CHILD_FIELD_1).queryOn(TestChildEntity.INSTANCE).askedBy(TestGrandChildEntity.INSTANCE).build()
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

    private <T> FieldValueSupplier<T> supplierRequiring(EntityField<?, ?> requestedField) {
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
        return fieldsToFetchBuilder.build(ImmutableList.of(cmd.get()), flowConfig);
    }

}