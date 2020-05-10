package com.kenshoo.pl.entity;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.kenshoo.jooq.AbstractDataTable;
import com.kenshoo.jooq.DataTable;
import com.kenshoo.jooq.DataTableUtils;
import com.kenshoo.jooq.TestJooqConfig;
import com.kenshoo.pl.entity.mysql.MySqlDeadlockDetector;
import com.kenshoo.pl.entity.spi.OutputGenerator;
import org.apache.commons.lang3.RandomStringUtils;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;
import org.junit.*;

import java.sql.SQLException;
import java.util.Collection;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

@Ignore
public class DeadlockTest {

    private static final int ID = 1;

    private static final Object[][] DATA = {
            {ID, "Alpha"},
    };

    private DSLContext dslContext = TestJooqConfig.create();

    private PersistenceLayer<TestEntityType> persistenceLayer;

    private static TestTable table1;
    private static TestTable table2;
    private static TestEntityType entityType;
    private static DSLContext staticDSLContext;

    @AfterClass
    public static void dropTables() {
        staticDSLContext.dropTableIfExists(table1).execute();
        staticDSLContext.dropTableIfExists(table2).execute();
    }

    @Before
    public void setup() {

        persistenceLayer = new PersistenceLayer<>(dslContext);

        if (table1 == null) {
            String tableName1 = RandomStringUtils.randomAlphanumeric(15);
            table1 = new TestTable(tableName1);
            DataTableUtils.createTable(dslContext, table1);

            String tableName2 = RandomStringUtils.randomAlphanumeric(15);
            table2 = new TestTable(tableName2);
            DataTableUtils.createTable(dslContext, table2);

            entityType = new TestEntityType();

            staticDSLContext = dslContext;
        }
        DataTableUtils.populateTable(dslContext, table1, DATA);
        DataTableUtils.populateTable(dslContext, table2, DATA);

    }

    @After
    public void clearTables() {
        dslContext.deleteFrom(table1).execute();
        dslContext.deleteFrom(table2).execute();
    }

    @Test
    public void deadlockWithNoRetries() throws InterruptedException {
        DeadlockRetryer dataBaseRetryer = createDataBaseRetryer(1);
        PLContext plContext = new PLContext.Builder(dslContext).withRetryer(dataBaseRetryer).build();
        AtomicReference<Exception> exception = runTwoLockingThreads(plContext);
        assertNotNull(exception.get());
        assertThat(Throwables.getRootCause(exception.get()), is(instanceOf(SQLException.class)));
    }

    @Test
    public void noDeadlockWithRetries() throws InterruptedException {
        DeadlockRetryer dataBaseRetryer = createDataBaseRetryer(3);
        PLContext plContext = new PLContext.Builder(dslContext).withRetryer(dataBaseRetryer).build();
        AtomicReference<Exception> exception = runTwoLockingThreads(plContext);
        assertNull(exception.get());
    }

    private AtomicReference<Exception> runTwoLockingThreads(PLContext plContext) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CyclicBarrier barrier = new CyclicBarrier(2);
        AtomicReference<Exception> exception = new AtomicReference<>();
        OutputGenerator<TestEntityType> outputGenerator1 = new DeadlockOutputGenerator(table1, table2, barrier);
        ChangeFlowConfig<TestEntityType> flowConfig1 = ChangeFlowConfigBuilderFactory.newInstance(plContext, entityType).withoutOutputGenerators().withOutputGenerator(outputGenerator1).build();
        executorService.submit(() -> {
            try {
                persistenceLayer.update(ImmutableList.of(new UpdateEntityCommand<>(entityType, new TestEntityType.Key(ID))), flowConfig1);
                return;
            } catch (Exception e) {
                exception.set(e);
            }
        });
        OutputGenerator<TestEntityType> outputGenerator2 = new DeadlockOutputGenerator(table2, table1, barrier);
        ChangeFlowConfig<TestEntityType> flowConfig2 = ChangeFlowConfigBuilderFactory.newInstance(plContext, entityType).withoutOutputGenerators().withOutputGenerator(outputGenerator2).build();
        executorService.submit(() -> {
            try {
                persistenceLayer.update(ImmutableList.of(new UpdateEntityCommand<>(entityType, new TestEntityType.Key(ID))), flowConfig2);
                return;
            } catch (Exception e) {
                exception.set(e);
            }
        });
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.SECONDS);
        return exception;
    }

    private DeadlockRetryer createDataBaseRetryer(int maxDeadlockRetries) {
        DeadlockRetryer dataBaseRetryer = new DeadlockRetryer(new MySqlDeadlockDetector(), () -> false);
        dataBaseRetryer.setMaxDeadlockRetries(maxDeadlockRetries);
        dataBaseRetryer.setFirstSleepBetweenRetriesMillis(50);
        return dataBaseRetryer;
    }

    private static class TestTable extends AbstractDataTable<TestTable> {

        private final TableField<Record, Integer> id = createPKField("id", SQLDataType.INTEGER);
        private final TableField<Record, String> field = createField("field", SQLDataType.VARCHAR.length(50));

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


    public static class TestEntityType extends AbstractEntityType<TestEntityType> {

        public static final TestEntityType INSTANCE = new TestEntityType();

        public static final EntityField<TestEntityType, Integer> ID = INSTANCE.field(table1.id);
        public static final EntityField<TestEntityType, String> FIELD = INSTANCE.field(table1.field);

        protected TestEntityType() {
            super("test");
        }

        @Override
        public DataTable getPrimaryTable() {
            return table1;
        }

        public static class Key extends SingleUniqueKeyValue<TestEntityType, Integer> {
            public static final SingleUniqueKey<TestEntityType, Integer> DEFINITION = new SingleUniqueKey<TestEntityType, Integer>(ID) {
                @Override
                protected SingleUniqueKeyValue<TestEntityType, Integer> createValue(Integer value) {
                    return new Key(value);
                }
            };

            public Key(int val) {
                super(DEFINITION, val);
            }
        }
    }

    private class DeadlockOutputGenerator implements OutputGenerator<TestEntityType> {

        private final TestTable table1;
        private final TestTable table2;
        private final CyclicBarrier barrier;
        private boolean waitForBarrier = true;

        public DeadlockOutputGenerator(TestTable table1, TestTable table2, CyclicBarrier barrier) {
            this.table1 = table1;
            this.table2 = table2;
            this.barrier = barrier;
        }

        @Override
        public void generate(Collection<? extends EntityChange<TestEntityType>> entityChanges, ChangeOperation changeOperation, ChangeContext changeContext) {
            dslContext.update(table1).set(table1.field, "test").where(table1.id.eq(1)).execute();
            try {
                if (waitForBarrier) {
                    waitForBarrier = false;
                    barrier.await();
                }
            } catch (Exception e) {
                throw Throwables.propagate(e);
            }
            dslContext.update(table2).set(table2.field, "test").where(table2.id.eq(1)).execute();
        }

        @Override
        public Stream<? extends EntityField<?, ?>> requiredFields(Collection<? extends EntityField<TestEntityType, ?>> fieldsToUpdate, ChangeOperation changeOperation) {
            return Stream.empty();
        }
    }
}
