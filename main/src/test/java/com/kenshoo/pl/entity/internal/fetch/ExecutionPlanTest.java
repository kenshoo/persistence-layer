package com.kenshoo.pl.entity.internal.fetch;

import com.kenshoo.pl.entity.SecondaryTable;
import com.kenshoo.pl.entity.TestChildEntityTable;
import com.kenshoo.pl.entity.TestEntity;
import com.kenshoo.pl.entity.TestEntityTable;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.*;

public class ExecutionPlanTest {

    @Test
    public void secondary_table_is_fetched_as_1_to_1() {
        ExecutionPlan plan = new ExecutionPlan(TestEntityTable.TABLE, asList(
                TestEntity.SECONDARY_FIELD_1
        ));

        assertThat(plan.getManyToOnePlans(), empty());
        assertThat(first(plan.getOneToOnePlan().getSecondaryTableRelations()).getSecondary(), is(SecondaryTable.TABLE));
    }

    @Test
    public void find_secondary_of_parent_as_1_to_1() {
        ExecutionPlan plan = new ExecutionPlan(TestChildEntityTable.TABLE, asList(
                TestEntity.SECONDARY_FIELD_1
        ));

        assertThat(plan.getManyToOnePlans(), empty());
        assertThat(first(plan.getOneToOnePlan().getSecondaryTableRelations()).getSecondary(), is(SecondaryTable.TABLE));
    }

    private static <T> T first(Iterable<T> items) {
        return items.iterator().next();
    }

}

