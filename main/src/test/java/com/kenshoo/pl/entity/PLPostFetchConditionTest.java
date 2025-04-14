package com.kenshoo.pl.entity;

import com.google.common.collect.ImmutableSet;
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
public class PLPostFetchConditionTest {

    @Mock
    private Predicate<Entity> postFetchCondition1;
    @Mock
    private Predicate<Entity> postFetchCondition2;
    @Mock
    private Predicate<Entity> outputPostFetchCondition;
    @Mock
    private EntityField<?, ?> entityField1;
    @Mock
    private EntityField<?, ?> entityField2;

    private Set<EntityField<?, ?>> entityFields;
    private PLPostFetchCondition plPostFetchCondition1;
    private PLPostFetchCondition plPostFetchCondition2;
    private PLPostFetchCondition binaryPLPostFetchCondition;

    @Before
    public void setUp() {
        entityFields = ImmutableSet.of(entityField1, entityField2);

        plPostFetchCondition1 = new PLPostFetchCondition(postFetchCondition1, entityField1);
        plPostFetchCondition2 = new PLPostFetchCondition(postFetchCondition2, entityField2);
        binaryPLPostFetchCondition = new PLPostFetchCondition(outputPostFetchCondition, entityFields);
    }

    // TODO - test the predicate directly

    @Test
    public void testGetFields() {
        assertThat(new PLPostFetchCondition(postFetchCondition1, entityFields).getFields(), equalTo(entityFields));
    }

    @Test
    public void testAndProducesCorrectPLPostFetchCondition() {
        when(postFetchCondition1.and(postFetchCondition2)).thenReturn(outputPostFetchCondition);

        final var outputPlPostFetchCondition = plPostFetchCondition1.and(plPostFetchCondition2);

        // TODO check 'and' condition
        assertThat(outputPlPostFetchCondition.getFields(), equalTo(binaryPLPostFetchCondition.getFields()));
    }

    @Test
    public void testOrProducesCorrectPLPostFetchCondition() {
        when(postFetchCondition1.or(postFetchCondition2)).thenReturn(outputPostFetchCondition);

        final var outputPlPostFetchCondition = plPostFetchCondition1.or(plPostFetchCondition2);

        assertThat(outputPlPostFetchCondition.getFields(), equalTo(binaryPLPostFetchCondition.getFields()));
    }

    @Test
    public void testNotProducesCorrectPLCondition() {
        when(postFetchCondition1.negate()).thenReturn(postFetchCondition2);

        final var outputPlPostFetchCondition = PLPostFetchCondition.not(plPostFetchCondition1);

        // TODO check 'not' condition
        assertThat(outputPlPostFetchCondition.getFields(), equalTo(plPostFetchCondition1.getFields()));
    }
}
