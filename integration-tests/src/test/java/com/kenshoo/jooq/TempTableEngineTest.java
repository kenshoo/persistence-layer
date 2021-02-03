package com.kenshoo.jooq;

import com.google.common.collect.ImmutableList;
import com.kenshoo.pl.entity.TestEntityTable;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.TableField;
import org.junit.Test;

import static com.kenshoo.jooq.TestJooqConfig.alwaysAllocatingNewConnections;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;


public class TempTableEngineTest {

    private final TestEntityTable table = TestEntityTable.TABLE;
    private final DSLContext jooq = TestJooqConfig.create();

    @Test
    public void two_temp_tables_can_simultaneously_co_exist_each_one_on_different_thread() throws Exception {

        TempTableResource<TestEntityTable> tmpTable1 = TempTableEngine.tempTable(jooq, table, new Field<?>[] { table.id }, populate(table.id, 10));

        int idSelectedFromTable2 = supplyAsync(() -> {
            TempTableResource<TestEntityTable> tmpTable2 = TempTableEngine.tempTable(jooq, table, new Field<?>[] { table.id }, populate(table.id, 20));
            return selectIdFrom(jooq, tmpTable2);
        }).get();

        int idSelectedFromTable1 = selectIdFrom(jooq, tmpTable1);

        assertThat(idSelectedFromTable1, is(10));
        assertThat(idSelectedFromTable2, is(20));
    }

    private Integer selectIdFrom(DSLContext jooq, TempTableResource<TestEntityTable> tmpTable) {
        return jooq.select(table.id).from(tmpTable.getTable()).fetchOne().get(table.id);
    }

    private <T> TablePopulator populate(TableField<Record, T> field, T value) {
        return new TempTableCreator.TempTablePopulator(ImmutableList.of(
                new FieldAndValues<>(field, ImmutableList.of(value))
        ));
    }


}
