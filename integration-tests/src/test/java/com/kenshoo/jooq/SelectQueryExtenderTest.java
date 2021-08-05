package com.kenshoo.jooq;

import com.google.common.collect.ImmutableList;
import org.hamcrest.Matchers;
import org.jooq.*;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.jooq.lambda.tuple.Tuple5;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SelectQueryExtenderTest {

    private DSLContext dslContext = TestJooqConfig.create();

    @Before
    public void setupTable() {
        dslContext.dropTableIfExists(TestTable.TABLE).execute();
        TestTable testTable = TestTable.TABLE;
        dslContext.createTable(testTable).column(testTable.id, testTable.id.getDataType())
                .column(testTable.field, testTable.field.getDataType())
                .column(testTable.id_in_target, testTable.id_in_target.getDataType())
                .column(testTable.field1, testTable.field1.getDataType())
                .column(testTable.field2, testTable.field2.getDataType())
                .execute();
        BatchBindStep batch = dslContext.batch(dslContext.insertInto(testTable, testTable.fields()).values(new Object[testTable.fields().length]));
        for (int i = 0; i < 1000; i++) {
            batch.bind(i, i * 100, Integer.toString(i), "1", TestE.test1);
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
        List<Integer> idsList = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            idsList.add(i);
        }
        SelectJoinStep<Record1<Integer>> step = dslContext.select(table.field).from(table);
        try (QueryExtension<SelectJoinStep<Record1<Integer>>> queryExtension = SelectQueryExtender.of(dslContext, step).withCondition(table.id).in(idsList)) {
            List<Integer> values = queryExtension.getQuery().fetch(table.field);
            assertThat(values.size(), is(1000));
        }
    }

    @Test
    public void testStringJoin() {
        TestTable table = TestTable.TABLE;
        List<String> idsList = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            idsList.add(Integer.toString(i));
        }
        SelectJoinStep<Record1<Integer>> step = dslContext.select(table.field).from(table);
        try (QueryExtension<SelectJoinStep<Record1<Integer>>> queryExtension = SelectQueryExtender.of(dslContext, step).withCondition(table.id_in_target).in(idsList)) {
            List<Integer> values = queryExtension.getQuery().fetch(table.field);
            assertThat(values.size(), is(1000));
        }
    }

    @Test
    public void testJoinWithCondition() {
        TestTable table = TestTable.TABLE;
        List<Integer> idsList = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            idsList.add(i);
        }
        SelectConditionStep<Record1<Integer>> step = dslContext.select(table.field).from(table).where(table.field.eq(100));
        try (QueryExtension<SelectConditionStep<Record1<Integer>>> queryExtension = SelectQueryExtender.of(dslContext, step).withCondition(table.id).in(idsList)) {
            List<Integer> values = queryExtension.getQuery().fetch(table.field);
            assertThat(values.size(), is(1));
            assertThat(values.get(0), is(100));
        }
    }

    @Test
    public void testIn() {
        TestTable table = TestTable.TABLE;
        List<Integer> idsList = ImmutableList.of(1, 2);
        SelectJoinStep<Record1<Integer>> step = dslContext.select(table.field).from(table);
        try (QueryExtension<SelectJoinStep<Record1<Integer>>> queryExtension = SelectQueryExtender.of(dslContext, step).withCondition(table.id).in(idsList)) {
            List<Integer> values = queryExtension.getQuery().fetch(table.field);
            assertThat(values.size(), is(2));
        }
    }

    @Test
    public void testInWithCondition() {
        TestTable table = TestTable.TABLE;
        List<Integer> idsList = ImmutableList.of(0, 1, 2);
        SelectConditionStep<Record1<Integer>> step = dslContext.select(table.field).from(table).where(table.field.eq(100));
        try (QueryExtension<SelectConditionStep<Record1<Integer>>> queryExtension = SelectQueryExtender.of(dslContext, step).withCondition(table.id).in(idsList)) {
            List<Integer> values = queryExtension.getQuery().fetch(table.field);
            assertThat(values.size(), is(1));
            assertThat(values.get(0), is(100));
        }
    }

    @Test
    public void testTwoFieldsLookup() {
        TestTable table = TestTable.TABLE;
        List<Tuple2<Integer, String>> ids = new ArrayList<>();
        ids.add(new Tuple2<>(5, "5"));
        ids.add(new Tuple2<>(30, "3"));
        ids.add(new Tuple2<>(5, "50"));
        ids.add(new Tuple2<>(50, "50"));
        SelectJoinStep<Record2<Integer, Integer>> step = dslContext.select(table.id, table.field).from(table);
        try (QueryExtension<SelectJoinStep<Record2<Integer, Integer>>> queryExtension = SelectQueryExtender.of(dslContext, step).withCondition(table.id, table.id_in_target).in(ids)) {
            List<Integer> values = queryExtension.getQuery().fetch(table.id);
            assertThat(values.size(), is(2));
            assertThat(values, containsInAnyOrder(5, 50));
        }
    }

    @Test
    public void testTwoFieldsLookupDuplicate() {
        TestTable table = TestTable.TABLE;
        List<Tuple2<Integer, String>> ids = new ArrayList<>();
        ids.add(new Tuple2<>(5, "5"));
        ids.add(new Tuple2<>(30, "3"));
        ids.add(new Tuple2<>(5, "5"));
        ids.add(new Tuple2<>(50, "50"));
        SelectJoinStep<Record2<Integer, Integer>> step = dslContext.select(table.id, table.field).from(table);
        try (QueryExtension<SelectJoinStep<Record2<Integer, Integer>>> queryExtension = SelectQueryExtender.of(dslContext, step).withCondition(table.id, table.id_in_target).in(ids)) {
            List<Integer> values = queryExtension.getQuery().fetch(table.id);
            assertThat(values.size(), is(2));
            assertThat(values, containsInAnyOrder(5, 50));
        }
    }

    @Test
    public void testThreeFieldsLookup() {
        TestTable table = TestTable.TABLE;
        List<Tuple3<Integer, String, Integer>> ids = new ArrayList<>();
        ids.add(new Tuple3<>(5, "5", 500));
        ids.add(new Tuple3<>(30, "3", 3000));
        ids.add(new Tuple3<>(6, "5", 500));
        ids.add(new Tuple3<>(3, "3", 200));
        ids.add(new Tuple3<>(30, "30", 3000));
        SelectJoinStep<Record2<Integer, Integer>> step = dslContext.select(table.id, table.field).from(table);
        try (QueryExtension<SelectJoinStep<Record2<Integer, Integer>>> queryExtension = SelectQueryExtender.of(dslContext, step).withCondition(table.id, table.id_in_target, table.field).in(ids)) {
            List<Integer> values = queryExtension.getQuery().fetch(table.id);
            assertThat(values.size(), is(2));
            assertThat(values, containsInAnyOrder(5, 30));
        }
    }

    @Test
    public void testFourFieldsLookup() {
        TestTable table = TestTable.TABLE;
        List<Tuple4<Integer, String, Integer, TestE>> ids = new ArrayList<>();
        ids.add(new Tuple4<>(5, "5", 500, TestE.test1));
        ids.add(new Tuple4<>(30, "3", 3000, TestE.test1));
        ids.add(new Tuple4<>(6, "5", 500, TestE.test1));
        ids.add(new Tuple4<>(3, "3", 200,TestE.test1));
        ids.add(new Tuple4<>(30, "30", 3000, TestE.test1));
        SelectJoinStep<Record2<Integer, Integer>> step = dslContext.select(table.id, table.field).from(table);
        try (QueryExtension<SelectJoinStep<Record2<Integer, Integer>>> queryExtension = SelectQueryExtender.of(dslContext, step).withCondition(table.id, table.id_in_target, table.field, table.field2).in(ids)) {
            List<Integer> values = queryExtension.getQuery().fetch(table.id);
            assertThat(values.size(), is(2));
            assertThat(values, containsInAnyOrder(5, 30));
        }
    }

    @Test
    public void testFiveFieldsLookup() {
        TestTable table = TestTable.TABLE;
        List<Tuple5<Integer, String, Integer, String, TestE>> ids = new ArrayList<>();
        ids.add(new Tuple5<>(5, "5", 500, "1", TestE.test1));
        ids.add(new Tuple5<>(30, "3", 3000, "1", TestE.test1));
        ids.add(new Tuple5<>(6, "5", 500, "1", TestE.test1));
        ids.add(new Tuple5<>(3, "3", 200, "1",TestE.test1));
        ids.add(new Tuple5<>(30, "30", 3000, "1", TestE.test1));
        SelectJoinStep<Record2<Integer, Integer>> step = dslContext.select(table.id, table.field).from(table);
        try (QueryExtension<SelectJoinStep<Record2<Integer, Integer>>> queryExtension = SelectQueryExtender.of(dslContext, step).withCondition(table.id, table.id_in_target, table.field, table.field1, table.field2).in(ids)) {
            List<Integer> values = queryExtension.getQuery().fetch(table.id);
            assertThat(values.size(), is(2));
            assertThat(values, containsInAnyOrder(5, 30));
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
        final Field<String> field1 = createField("field1", SQLDataType.VARCHAR.length(10));
        final Field<TestE> field2 = createField("field2", SQLDataType.INTEGER.asConvertedDataType(new TestEConverter()));


        private TestTable() {
            super("ids_list_test");
        }
    }

    public enum TestE {
        test1,
        test2
    }

    private static class TestEConverter implements Converter<Integer, TestE> {
        public TestE from(Integer ordinal) {
            return ordinal == null ? null : TestE.values()[ordinal];
        }

        public Integer to(TestE e) {
            return e != null ? e.ordinal() : null;
        }

        public Class<Integer> fromType() {
            return Integer.class;
        }

        public Class<TestE> toType() {
            return TestE.class;
        }
    }
}