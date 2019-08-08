package com.kenshoo.pl.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.kenshoo.jooq.AbstractDataTable;
import com.kenshoo.jooq.DataTableUtils;
import com.kenshoo.jooq.TestJooqConfig;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.*;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;

public class CommandsExecutorTest {

    private static final Object[][] DATA = {
            {1, "Alpha", 10},
            {2, "Bravo", 20},
            {3, "Charlie", 30},
            {4, "Delta", 40},
    };

    private DSLContext dslContext = TestJooqConfig.create();

    private CommandsExecutor commandsExecutor;

    private TestTable table;
    
    @Before
    public void setup() {
        String tableName = RandomStringUtils.randomAlphanumeric(15);
        table = new TestTable(tableName);
        DataTableUtils.createTable(dslContext, table);
        dslContext.execute(String.format("ALTER TABLE %s MODIFY COLUMN %s %s auto_increment",
                table.getName(),
                table.id.getName(),
                table.id.getDataType().getTypeName()));
        DataTableUtils.populateTable(dslContext, table, DATA);
        commandsExecutor = CommandsExecutor.of(dslContext);
    }

    @After
    public void tearDown() {
        dslContext.dropTable(table);
    }

    @Test
    public void oneUpdate() {
        TestUpdateRecordCommand command = new TestUpdateRecordCommand(1);
        String valueToSet = "Alpha-1";
        command.set(table.field1, valueToSet);
        AffectedRows affectedRows = commandsExecutor.executeUpdates(table, Collections.singletonList(command));
        assertThat(affectedRows.getUpdated(), is(1));
        String actualValue = dslContext.select(table.field1).from(table).where(table.id.eq(1)).fetchOne(table.field1, String.class);
        assertThat(actualValue, is(valueToSet));
    }

    @Test
    public void heterogeneousUpdates() {
        TestUpdateRecordCommand command1 = new TestUpdateRecordCommand(1);
        String valueToSet1 = "Alpha-1";
        command1.set(table.field1, valueToSet1);
        TestUpdateRecordCommand command2 = new TestUpdateRecordCommand(2);
        Integer valueToSet2 = 100;
        command2.set(table.field2, valueToSet2);
        TestUpdateRecordCommand command3 = new TestUpdateRecordCommand(3);
        String valueToSet3 = "Charlie-1";
        command3.set(table.field1, valueToSet3);
        AffectedRows affectedRows = commandsExecutor.executeUpdates(table, ImmutableList.of(command1, command2, command3));
        assertThat(affectedRows.getUpdated(), is(3));

        Map<String, Object> actualValues1 = dslContext.select(table.field1, table.field2).from(table).where(table.id.eq(1)).fetchOneMap();
        assertThat(actualValues1, Matchers.<Map<String, Object>>is(ImmutableMap.<String, Object>of(table.field1.getName(), valueToSet1, table.field2.getName(), 10)));
        Map<String, Object> actualValues2 = dslContext.select(table.field1, table.field2).from(table).where(table.id.eq(2)).fetchOneMap();
        assertThat(actualValues2, Matchers.<Map<String, Object>>is(ImmutableMap.<String, Object>of(table.field1.getName(), "Bravo", table.field2.getName(), valueToSet2)));
        Map<String, Object> actualValues3 = dslContext.select(table.field1, table.field2).from(table).where(table.id.eq(3)).fetchOneMap();
        assertThat(actualValues3, Matchers.<Map<String, Object>>is(ImmutableMap.<String, Object>of(table.field1.getName(), valueToSet3, table.field2.getName(), 30)));
    }

    @Test
    public void homogeneousUpdates() {
        TestUpdateRecordCommand command1 = new TestUpdateRecordCommand(1);
        String valueToSet1 = "Alpha-1";
        command1.set(table.field1, valueToSet1);
        TestUpdateRecordCommand command2 = new TestUpdateRecordCommand(2);
        String valueToSet2 = "Bravo-2";
        command2.set(table.field1, valueToSet2);
        AffectedRows affectedRows = commandsExecutor.executeUpdates(table, ImmutableList.of(command1, command2));
        assertThat(affectedRows.getUpdated(), is(2));
        Map<Integer, String> actualValues = dslContext.select(table.id, table.field1).from(table).where(table.id.in(1, 2)).fetchMap(table.id, table.field1);
        assertThat(actualValues, Matchers.<Map<Integer, String>>is(ImmutableMap.of(1, valueToSet1, 2, valueToSet2)));
    }

    @Test
    public void oneInsert() {
        TestCreateRecordCommand command = new TestCreateRecordCommand();
        command.set(table.id, 5);
        command.set(table.field2, 20);
        command.set(table.field1, "Echo");
        command.set(table.field2, 50); // setting another value to the same field
        AffectedRows affectedRows = commandsExecutor.executeInserts(table, Collections.singletonList(command));
        assertThat(affectedRows.getInserted(), is(1));
        assertThat(affectedRows.getUpdated(), is(0));

        Map<String, Object> actualValues1 = dslContext.select(table.field1, table.field2).from(table).where(table.id.eq(5)).fetchOneMap();
        assertThat(actualValues1, Matchers.<Map<String, Object>>is(ImmutableMap.<String, Object>of(table.field1.getName(), "Echo", table.field2.getName(), 50)));
    }

    @Test
    public void retrieveGeneratedId() {
        List<TestCreateRecordCommand> commands = Arrays.asList(
                new TestCreateRecordCommand().with(table.field1, "five"),
                new TestCreateRecordCommand().with(table.field1, "six"),
                new TestCreateRecordCommand().with(table.field1, "seven"),
                new TestCreateRecordCommand().with(table.field1, "eight")
        );

        AffectedRows affectedRows = commandsExecutor.insertAndGetGeneratedIds(table, commands);

        assertThat(affectedRows.getGeneratedIds(), contains(5L, 6L, 7L, 8L));

        List<String> rowsInDB = dslContext.selectFrom(table).where(table.id.ge(5))
                .fetch(rec -> "id: " + rec.get(table.id) + ", field1: " + rec.get(table.field1));

        assertThat(rowsInDB, contains(
                "id: 5, field1: five",
                "id: 6, field1: six",
                "id: 7, field1: seven",
                "id: 8, field1: eight"
        ));
    }

    @Ignore // our next task
    @Test
    public void retrieveGeneratedIdsInOrder() {
        List<TestCreateRecordCommand> commands = Arrays.asList(
                new TestCreateRecordCommand().with(table.field1, "five"),
                new TestCreateRecordCommand().with(table.field1, "six").with(table.field2, 99),
                new TestCreateRecordCommand().with(table.field1, "seven")
        );

        AffectedRows affectedRows = commandsExecutor.insertAndGetGeneratedIds(table, commands);

        assertThat(affectedRows.getGeneratedIds(), contains(5L, 6L, 7L));

        List<String> rowsInDB = dslContext.selectFrom(table).where(table.id.ge(5))
                .fetch(rec -> "id: " + rec.get(table.id) + ", field1: " + rec.get(table.field1));

        assertThat(rowsInDB, contains(
                "id: 5, field1: five",
                "id: 6, field1: six",
                "id: 7, field1: seven"
        ));
    }

    @Test
    public void homogeneousInserts() {
        TestCreateRecordCommand command1 = new TestCreateRecordCommand();
        command1.set(table.id, 5);
        command1.set(table.field1, "Echo");
        command1.set(table.field2, 50);
        TestCreateRecordCommand command2 = new TestCreateRecordCommand();
        command2.set(table.id, 6);
        command2.set(table.field1, "Foxtrot");
        command2.set(table.field2, 60);
        AffectedRows affectedRows = commandsExecutor.executeInserts(table, ImmutableList.of(command1, command2));
        assertThat(affectedRows.getInserted(), is(2));
        assertThat(affectedRows.getUpdated(), is(0));

        Map<String, Object> actualValues1 = dslContext.select(table.field1, table.field2).from(table).where(table.id.eq(5)).fetchOneMap();
        assertThat(actualValues1, Matchers.<Map<String, Object>>is(ImmutableMap.<String, Object>of(table.field1.getName(), "Echo", table.field2.getName(), 50)));
        Map<String, Object> actualValues2 = dslContext.select(table.field1, table.field2).from(table).where(table.id.eq(6)).fetchOneMap();
        assertThat(actualValues2, Matchers.<Map<String, Object>>is(ImmutableMap.<String, Object>of(table.field1.getName(), "Foxtrot", table.field2.getName(), 60)));
    }

    @Test
    public void homogeneousInsertsDifferentOrder() {
        TestCreateRecordCommand command1 = new TestCreateRecordCommand();
        command1.set(table.id, 5);
        command1.set(table.field1, "Echo");
        command1.set(table.field2, 50);
        TestCreateRecordCommand command2 = new TestCreateRecordCommand();
        command2.set(table.id, 6);
        command2.set(table.field2, 60);
        command2.set(table.field1, "Foxtrot");
        AffectedRows affectedRows = commandsExecutor.executeInserts(table, ImmutableList.of(command1, command2));
        assertThat(affectedRows.getInserted(), is(2));
        assertThat(affectedRows.getUpdated(), is(0));

        Map<String, Object> actualValues1 = dslContext.select(table.field1, table.field2).from(table).where(table.id.eq(5)).fetchOneMap();
        assertThat(actualValues1, Matchers.<Map<String, Object>>is(ImmutableMap.<String, Object>of(table.field1.getName(), "Echo", table.field2.getName(), 50)));
        Map<String, Object> actualValues2 = dslContext.select(table.field1, table.field2).from(table).where(table.id.eq(6)).fetchOneMap();
        assertThat(actualValues2, Matchers.<Map<String, Object>>is(ImmutableMap.<String, Object>of(table.field1.getName(), "Foxtrot", table.field2.getName(), 60)));
    }

    @Test
    public void heterogeneousInserts() {
        TestCreateRecordCommand command1 = new TestCreateRecordCommand();
        command1.set(table.id, 5);
        command1.set(table.field1, "Echo");
        TestCreateRecordCommand command2 = new TestCreateRecordCommand();
        command2.set(table.id, 6);
        command2.set(table.field2, 60);
        AffectedRows affectedRows = commandsExecutor.executeInserts(table, ImmutableList.of(command1, command2));
        assertThat(affectedRows.getInserted(), is(2));
        assertThat(affectedRows.getUpdated(), is(0));

        Map<String, Object> actualValues1 = dslContext.select(table.field1, table.field2).from(table).where(table.id.eq(5)).fetchOneMap();
        Map<String, Object> expectedValues1 = new HashMap<>();
        expectedValues1.put(table.field1.getName(), "Echo");
        expectedValues1.put(table.field2.getName(), null);
        assertThat(actualValues1, is(expectedValues1));
        Map<String, Object> actualValues2 = dslContext.select(table.field1, table.field2).from(table).where(table.id.eq(6)).fetchOneMap();
        Map<String, Object> expectedValues2 = new HashMap<>();
        expectedValues2.put(table.field1.getName(), null);
        expectedValues2.put(table.field2.getName(), 60);
        assertThat(actualValues2, is(expectedValues2));
    }

    @Test(expected = Exception.class)
    public void insertDuplicateKey() {
        TestCreateRecordCommand command = new TestCreateRecordCommand();
        String valueToSet = "Alpha-1";
        command.set(table.id, 1);
        command.set(table.field1, valueToSet);
        AffectedRows affectedRows = commandsExecutor.executeInserts(table, Collections.singletonList(command));
        assertThat(affectedRows.getInserted(), is(0));
        assertThat(affectedRows.getUpdated(), is(1));
    }

    @Test
    public void onDuplicateKeyUpdate() {
        TestCreateRecordCommand command = new TestCreateRecordCommand();
        String valueToSet = "Alpha-1";
        command.set(table.id, 1);
        command.set(table.field1, valueToSet);
        AffectedRows affectedRows = commandsExecutor.executeInsertsOnDuplicateKeyUpdate(table, Collections.singletonList(command));
        assertThat(affectedRows.getInserted(), is(0));
        assertThat(affectedRows.getUpdated(), is(1));
        Integer count = dslContext.select(DSL.count()).from(table).where(table.id.eq(1)).fetchOne(0, Integer.class);
        assertThat(count, is(1));
        String actualValue = dslContext.select(table.field1).from(table).where(table.id.eq(1)).fetchOne(table.field1, String.class);
        assertThat(actualValue, is(valueToSet));
    }

    @Test
    public void onDuplicateKeyIgnore() {
        TestCreateRecordCommand command = new TestCreateRecordCommand();
        String valueToSet = "Alpha-1";
        command.set(table.id, 1);
        command.set(table.field1, valueToSet);
        AffectedRows affectedRows = commandsExecutor.executeInsertsOnDuplicateKeyIgnore(table, Collections.singletonList(command));
        assertThat(affectedRows.getInserted(), is(0));
        assertThat(affectedRows.getUpdated(), is(0));
        Integer count = dslContext.select(DSL.count()).from(table).where(table.id.eq(1)).fetchOne(0, Integer.class);
        assertThat(count, is(1));
        String actualValue = dslContext.select(table.field1).from(table).where(table.id.eq(1)).fetchOne(table.field1, String.class);
        assertThat(actualValue, is("Alpha")); // old value
    }

    @Test
    public void delete() {
        AffectedRows affectedRows = commandsExecutor.executeDeletes(table, ImmutableList.of(new TestDeleteRecordCommand(2), new TestDeleteRecordCommand(3), new TestDeleteRecordCommand(5)));
        assertThat(affectedRows.getDeleted(), is(2));
        List<Integer> idsLeft = dslContext.select(table.id).from(table).fetch(table.id);
        assertThat(idsLeft, containsInAnyOrder(1, 4));
    }

    private static class TestTable extends AbstractDataTable<TestTable> {

        private final TableField<Record, Integer> id = createPKField("id", SQLDataType.INTEGER);
        private final TableField<Record, String> field1 = createField("field1", SQLDataType.VARCHAR.length(50));
        protected final TableField<Record, Integer> field2 = createField("field2", SQLDataType.INTEGER);

        public TestTable(String name) {
            super(name);
        }

        public TestTable(TestTable aliased, String alias) {
            super(aliased, alias);
        }

        @Override
        public TestTable as(String alias) {
            return new TestTable(this, alias);
        }
    }

    private class TestUpdateRecordCommand extends UpdateRecordCommand {
        public TestUpdateRecordCommand(int id) {
            super(table, id);
        }
    }

    private class TestDeleteRecordCommand extends DeleteRecordCommand {
        public TestDeleteRecordCommand(int id) {
            super(table, id);
        }
    }

    private class TestCreateRecordCommand extends CreateRecordCommand {
        public TestCreateRecordCommand() {
            super(table);
        }

        public <T> TestCreateRecordCommand with(Field<T> field, T value) {
            super.set(field, value);
            return this;
        }
    }
}