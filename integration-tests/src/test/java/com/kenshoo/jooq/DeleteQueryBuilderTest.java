package com.kenshoo.jooq;

import org.jooq.BatchBindStep;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.SelectJoinStep;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.List;

import static com.kenshoo.jooq.DeleteQueryBuilderTest.TestTable.TABLE;
import static com.kenshoo.jooq.QueryExtension.JOIN_TEMP_TABLE_LIMIT;
import static org.hamcrest.Matchers.is;
import static org.jooq.lambda.Seq.range;
import static org.junit.Assert.assertThat;

public class DeleteQueryBuilderTest {

    private DSLContext dslContext = TestJooqConfig.create();

    private DeleteQueryBuilder deleteQueryBuilder;

    private static final int MAX_ITEMS = 1000;
    private static final List<Integer> idsList = range(0, MAX_ITEMS).toList();

    @Before
    public void setupTable() {
        deleteQueryBuilder = new DeleteQueryBuilder(dslContext);
        createTestTable(TABLE);
        assertThat(fetchByIds(TABLE, idsList).size(), is(MAX_ITEMS));
    }

    @After
    public void dropTable() {
        dslContext.dropTableIfExists(TABLE).execute();
    }


    @Test
    public void whenWeHaveMoreThan10ItemsInTheInUseJoin() {
        //noinspection unchecked
        try (DeleteQueryExtension queryExtension = deleteQueryBuilder
                .table(TABLE)
                .withCondition(TABLE.id).in(range(0, MAX_ITEMS - 1).toList())) {

            queryExtension.getQuery().execute();
        }

        assertThat(fetchByIds(TABLE, idsList).size(), is(1));
    }

    @Test
    public void whenWeHaveLessThan10ItemsInTheInUseWhere() {
        try (DeleteQueryExtension queryExtension = deleteQueryBuilder
                .table(TABLE)
                .withCondition(TABLE.id).in(range(0, JOIN_TEMP_TABLE_LIMIT).toList())) {

            queryExtension.getQuery().execute();
        }

        assertThat(fetchByIds(TABLE, idsList).size(), is(990));
    }

    private void createTestTable(TestTable testTable) {
        dslContext.dropTableIfExists(testTable).execute();
        dslContext.createTable(testTable)
                .column(testTable.id, testTable.id.getDataType())
                .execute();
        BatchBindStep batch = dslContext.batch(dslContext.insertInto(testTable, testTable.fields()).values(new Object[testTable.fields().length]));
        range(0, MAX_ITEMS).forEach(batch::bind);
        batch.execute();
    }

    private List<Integer> fetchByIds(final TestTable table, final List<Integer> idsList) {
        SelectJoinStep<Record1<Integer>> step = dslContext.select(table.id).from(table);
        try (QueryExtension<SelectJoinStep<Record1<Integer>>> queryExtension = SelectQueryExtender.of(dslContext, step).withCondition(table.id).in(idsList)) {
            return queryExtension.getQuery().fetch(table.id);
        }
    }

    static class TestTable extends TableImpl<Record> {

        static final TestTable TABLE = new TestTable();
        final Field<Integer> id = createField("id", SQLDataType.INTEGER);


        private TestTable() {
            super("delete_ids_list_test");
        }
    }
}
