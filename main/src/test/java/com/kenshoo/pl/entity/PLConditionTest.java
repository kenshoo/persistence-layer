package com.kenshoo.pl.entity;

import com.google.common.collect.ImmutableSet;
import org.jooq.Condition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Set;
import java.util.function.Predicate;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PLConditionTest {

    @Mock
    private Condition jooqCondition1;
    @Mock
    private Condition jooqCondition2;
    @Mock
    private Condition outputJooqCondition;
    @Mock
    private Predicate<Entity> lambdaCondition1;
    @Mock
    private Predicate<Entity> lambdaCondition2;
    @Mock
    private Predicate<Entity> outputLambdaCondition;
    @Mock
    private EntityField<?, ?> entityField1;
    @Mock
    private EntityField<?, ?> entityField2;

    private Set<EntityField<?, ?>> entityFields;
    private PLCondition plCondition1;
    private PLCondition plCondition2;
    private PLCondition binaryPLCondition;

    @Before
    public void setUp() {
        entityFields = ImmutableSet.of(entityField1, entityField2);

        plCondition1 = new PLCondition(jooqCondition1, lambdaCondition1, entityField1);
        plCondition2 = new PLCondition(jooqCondition2, lambdaCondition2, entityField2);
        binaryPLCondition = new PLCondition(outputJooqCondition, outputLambdaCondition, entityFields);
    }

    @Test
    public void testGetJooqCondition() {
        assertThat(plCondition1.getJooqCondition(), equalTo(jooqCondition1));
    }

    @Test
    public void testGetLambdaCondition() {
        assertThat(plCondition1.getPostFetchCondition(), equalTo(lambdaCondition1));
    }

    @Test
    public void testGetFields() {
        assertThat(new PLCondition(jooqCondition1, lambdaCondition1 ,entityFields).getFields(),
                   equalTo(entityFields));
    }

    @Test
    public void testAndProducesCorrectPLCondition() {
        when(jooqCondition1.and(jooqCondition2)).thenReturn(outputJooqCondition);
        when(lambdaCondition1.and(lambdaCondition2)).thenReturn(outputLambdaCondition);

        assertThat(plCondition1.and(plCondition2), equalTo(binaryPLCondition));
    }

    @Test
    public void testOrProducesCorrectPLCondition() {
        when(jooqCondition1.or(jooqCondition2)).thenReturn(outputJooqCondition);
        when(lambdaCondition1.or(lambdaCondition2)).thenReturn(outputLambdaCondition);

        assertThat(plCondition1.or(plCondition2), equalTo(binaryPLCondition));
    }

    @Test
    public void testNotProducesCorrectPLCondition() {
        when(jooqCondition1.not()).thenReturn(jooqCondition2);
        when(lambdaCondition1.negate()).thenReturn(lambdaCondition2);

        assertThat(PLCondition.not(plCondition1),
                equalTo(new PLCondition(jooqCondition2, lambdaCondition2, entityField1)));
    }
}
