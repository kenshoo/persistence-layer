package com.kenshoo.pl.entity.internal;

import com.google.common.collect.ImmutableList;
import com.kenshoo.pl.entity.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DbCommandsOutputGeneratorTest {

    @Mock
    private PLContext plContext;

    @Mock
    private UpdateEntityCommand<TestEntity,?> cmd;

    private DbCommandsOutputGenerator<TestEntity> dbCommandsOutputGenerator;

    @Before
    public void setup() {
        dbCommandsOutputGenerator = new DbCommandsOutputGenerator<>(TestEntity.INSTANCE, plContext);
    }

    @Test
    public void required_entity_for_create() {
        List<? extends EntityField<?, ?>> fields = dbCommandsOutputGenerator.requiredFields(Collections.emptyList(), ChangeOperation.CREATE).collect(Collectors.toList());
        assertThat(fields.size(), is(0));
    }

    @Test
    public void required_entity_for_update() {
        List<? extends EntityField<?, ?>> fields = dbCommandsOutputGenerator.requiredFields(ImmutableList.of(TestEntity.FIELD_1), ChangeOperation.UPDATE).collect(Collectors.toList());
        assertThat(fields.size(), is(0));
    }

    @Test
    public void required_entity_for_secondary_table_update() {
        List<? extends EntityField<?, ?>> fields = dbCommandsOutputGenerator.requiredFields(ImmutableList.of(TestEntity.SECONDARY_FIELD_1), ChangeOperation.UPDATE).collect(Collectors.toList());
        assertThat(fields.size(), is(1));
        assertThat(fields, containsInAnyOrder(TestEntity.ID));
    }

}