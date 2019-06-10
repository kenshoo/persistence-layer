package com.kenshoo.jooq;

import org.hamcrest.Matchers;
import org.jooq.BatchBindStep;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.SelectConditionStep;
import org.jooq.SelectJoinStep;
import org.jooq.UpdateSetMoreStep;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class IdsListTest {

    private DSLContext dslContext = TestJooqConfig.create();

    @Before
    public void setupTable() {
        dslContext.dropTableIfExists(TestTable.TABLE).execute();
        TestTable testTable = TestTable.TABLE;
        dslContext.createTable(testTable).column(testTable.id, testTable.id.getDataType())
                .column(testTable.field, testTable.field.getDataType())
                .column(testTable.id_in_target, testTable.id_in_target.getDataType())
                .execute();
        BatchBindStep batch = dslContext.batch(dslContext.insertInto(testTable, testTable.fields()).values(new Object[testTable.fields().length]));
        for (int i = 0; i < 1000; i++) {
            batch.bind(i, i * 100, Integer.toString(i));
        }
        batch.execute();
    }

    @After
    public void dropTable() {
        dslContext.dropTableIfExists(TestTable.TABLE).execute();
    }
    
    @Test
    public void testJoin() {
        TestTable table = TestTable.TABLE;
        try (IntIdsList idsList = new IntIdsList(dslContext)) {
            for (int i = 0; i < 1000; i++) {
                idsList.add(i);
            }
            SelectJoinStep<Record1<Integer>> step = dslContext.select(table.field).from(table);
            step = idsList.imposeOnQuery(step, table.id);
            List<Integer> values = step.fetch(table.field);
            assertThat(values.size(), is(1000));
        }
    }

    @Test
    public void testJoinDuplicateIds() {
        TestTable table = TestTable.TABLE;
        try (IntIdsList idsList = new IntIdsList(dslContext)) {
            for (int i = 0; i < 1000; i++) {
                idsList.add(i);
                idsList.add(i);
            }
            SelectJoinStep<Record1<Integer>> step = dslContext.select(table.field).from(table);
            step = idsList.imposeOnQuery(step, table.id);
            List<Integer> values = step.fetch(table.field);
            assertThat(values.size(), is(1000));
        }
    }

    @Test
    public void testStringJoin() {
        TestTable table = TestTable.TABLE;
        try (StringIdsList idsList = new StringIdsList(dslContext)) {
            for (int i = 0; i < 1000; i++) {
                idsList.add(Integer.toString(i));
            }
            SelectJoinStep<Record1<Integer>> step = dslContext.select(table.field).from(table);
            step = idsList.imposeOnQuery(step, table.id_in_target);
            List<Integer> values = step.fetch(table.field);
            assertThat(values.size(), is(1000));
        }
    }

    @Test
    public void testJoinWithCondition() {
        TestTable table = TestTable.TABLE;
        try (IntIdsList idsList = new IntIdsList(dslContext)) {
            for (int i = 0; i < 1000; i++) {
                idsList.add(i);
            }
            SelectConditionStep<Record1<Integer>> step = dslContext.select(table.field).from(table).where(table.field.eq(100));
            step = idsList.imposeOnQuery(step, table.id);
            List<Integer> values = step.fetch(table.field);
            assertThat(values.size(), is(1));
            assertThat(values.get(0), is(100));
        }
    }

    @Test
    public void testIn() {
        TestTable table = TestTable.TABLE;
        try (IntIdsList idsList = new IntIdsList(dslContext)) {
            idsList.add(1);
            idsList.add(2);
            SelectJoinStep<Record1<Integer>> step = dslContext.select(table.field).from(table);
            step = idsList.imposeOnQuery(step, table.id);
            List<Integer> values = step.fetch(table.field);
            assertThat(values.size(), is(2));
        }
    }

    @Test
    public void testInWithCondition() {
        TestTable table = TestTable.TABLE;
        try (IntIdsList idsList = new IntIdsList(dslContext)) {
            idsList.add(0);
            idsList.add(1);
            idsList.add(2);
            SelectConditionStep<Record1<Integer>> step = dslContext.select(table.field).from(table).where(table.field.eq(100));
            step = idsList.imposeOnQuery(step, table.id);
            List<Integer> values = step.fetch(table.field);
            assertThat(values.size(), is(1));
            assertThat(values.get(0), is(100));
        }
    }

    @Test
    public void testUpdateInWithCondition() {
        TestTable table = TestTable.TABLE;
        try (IntIdsList idsList = new IntIdsList(dslContext)) {
            idsList.add(0);
            idsList.add(1);
            idsList.add(2);
            UpdateSetMoreStep<Record> update = dslContext.update(table).set(table.field, 1);
            idsList.imposeOnUpdate(update, table.id).execute();
        }
        try (IntIdsList idsList = new IntIdsList(dslContext)) {
            for (int i = 0; i < 6; i++) {
                 idsList.add(i);
            }
            SelectJoinStep<Record1<Integer>> step = dslContext.select(table.field).from(table);
            step = idsList.imposeOnQuery(step, table.id);
            List<Integer> values = step.orderBy(table.id).fetch(table.field);
            assertThat(values.size(), is(6));
            assertThat(values, Matchers.contains(1, 1, 1, 300, 400, 500));
        }
    }

    private static class TestTable extends TableImpl<Record> {

        static final TestTable TABLE = new TestTable();
        final Field<Integer> id = createField("id", SQLDataType.INTEGER);
        final Field<Integer> field = createField("field", SQLDataType.INTEGER);
        final Field<String> id_in_target = createField("id_in_target", SQLDataType.VARCHAR.length(255));

        private TestTable() {
            super("ids_list_test");
        }
    }

}