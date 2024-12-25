package com.kenshoo.pl.data;

import com.google.common.collect.ImmutableList;
import com.kenshoo.jooq.AbstractDataTable;
import com.kenshoo.jooq.DataTableUtils;
import com.kenshoo.jooq.FieldAndValue;
import com.kenshoo.jooq.TestJooqConfig;
import org.apache.commons.lang3.RandomStringUtils;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class VirtualPartitionTest {

    private static final Object[][] DATA = {
            {1, 1, "Alpha"},
            {1, 2, "Bravo"},
            {2, 1, "Charlie"},
            {3, 3, "Delta"},
    };

    private DSLContext dslContext = TestJooqConfig.create();

    private CommandsExecutor commandsExecutor;

    private TestTable table;
    
    @Before
    public void setup() {
        String tableName = RandomStringUtils.randomAlphanumeric(15);
        table = new TestTable(tableName);
        DataTableUtils.createTable(dslContext, table);
        DataTableUtils.populateTable(dslContext, table, DATA);
        commandsExecutor = CommandsExecutor.of(dslContext);
    }

    @After
    public void tearDown() {
        dslContext.dropTable(table);
    }

    @Test
    public void update() {
        TestUpdateRecordCommand command = new TestUpdateRecordCommand(1);
        String valueToSet = "Alpha-1";
        command.set(table.field1, valueToSet);
        commandsExecutor.executeUpdates(table, Collections.singletonList(command));
        String actualValue = dslContext.select(table.field1).from(table).where(table.type.eq(1).and(table.id.eq(1))).fetchOneInto(String.class);
        assertThat(actualValue, is(valueToSet));
        String otherPartitionValue = dslContext.select(table.field1).from(table).where(table.type.eq(2).and(table.id.eq(1))).fetchOneInto(String.class);
        assertThat(otherPartitionValue, is("Charlie"));
    }

    @Test
    public void insert() {
        TestCreateRecordCommand command = new TestCreateRecordCommand();
        command.set(table.id, 3);
        command.set(table.field1, "Echo");
        commandsExecutor.executeInserts(table, Collections.singletonList(command));

        String actualValues = dslContext.select(table.field1).from(table).where(table.type.eq(1).and(table.id.eq(3))).fetchOneInto(String.class);
        assertThat(actualValues, is("Echo"));
    }

    private static class TestTable extends AbstractDataTable<TestTable> {

        private final TableField<Record, Integer> type = createPKField("type", SQLDataType.INTEGER.identity(true));
        private final TableField<Record, Integer> id = createPKField("id", SQLDataType.INTEGER.identity(true));
        private final TableField<Record, String> field1 = createField("field1", SQLDataType.VARCHAR.length(50));

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

        @Override
        public Collection<FieldAndValue<?>> getVirtualPartition() {
            return ImmutableList.<FieldAndValue<?>>of(new FieldAndValue<>(type, 1));
        }
    }

    private class TestUpdateRecordCommand extends UpdateRecordCommand {
        public TestUpdateRecordCommand(int id) {
            super(table, new DatabaseId(new TableField[]{table.id}, new Object[]{id}));
        }
    }

    private class TestCreateRecordCommand extends CreateRecordCommand {
        public TestCreateRecordCommand() {
            super(table);
        }
    }
}