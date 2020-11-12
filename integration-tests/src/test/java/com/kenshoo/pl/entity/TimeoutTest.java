package com.kenshoo.pl.entity;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

@Ignore
public class TimeoutTest {

    private static final int ID = 1;

    private static final Object[][] DATA = {
            {ID, "Alpha"},
    };

    private static final String UPDATE_VALUE = "Beta";
    private static final String RETRY_VALUE = "Gamma";

    private DSLContext dslContext = TestJooqConfig.create();

    private PersistenceLayer<TestEntityType> persistenceLayer;

    private static TestTable table1;
    private static TestEntityType entityType;
    private static DSLContext staticDSLContext;

    @AfterClass
    public static void dropTables() {
        staticDSLContext.dropTableIfExists(table1).execute();
    }

    @Before
    public void setup() {

        persistenceLayer = new PersistenceLayer<>(dslContext);

        if (table1 == null) {
            String tableName1 = RandomStringUtils.randomAlphanumeric(15);
            table1 = new TestTable(tableName1);
            DataTableUtils.createTable(dslContext, table1);

            entityType = new TestEntityType();
            staticDSLContext = dslContext;
        }
        DataTableUtils.populateTable(dslContext, table1, DATA);
    }

    @After
    public void clearTables() {
        dslContext.deleteFrom(table1).execute();
    }

    @Test
    public void timeoutOutWithNoRetries() throws InterruptedException {
        DeadlockRetryer dataBaseRetryer = createDataBaseRetryer(1);
        PLContext plContext = new PLContext.Builder(dslContext).withRetryer(dataBaseRetryer).build();
        AtomicReference<Exception> exception = runTwoLockingThreads(plContext);
        assertNotNull(exception.get());
        Throwable expectedException = Throwables.getRootCause(exception.get());
        assertThat(expectedException, is(instanceOf(SQLException.class)));
        assertThat(expectedException.getMessage(), containsString("Lock wait timeout exceeded"));
    }

    @Test
    public void noTimeoutOutWithRetries() throws InterruptedException {
        DeadlockRetryer dataBaseRetryer = createDataBaseRetryer(3);
        PLContext plContext = new PLContext.Builder(dslContext).withRetryer(dataBaseRetryer).build();
        AtomicReference<Exception> exception = runTwoLockingThreads(plContext);
        assertNull(exception.get());
    }

    private DeadlockRetryer createDataBaseRetryer(int maxDeadlockRetries) {
        DeadlockRetryer dataBaseRetryer = new DeadlockRetryer(new MySqlDeadlockDetector(), () -> false);
        dataBaseRetryer.setMaxDeadlockRetries(maxDeadlockRetries);
        dataBaseRetryer.setFirstSleepBetweenRetriesMillis(50);
        return dataBaseRetryer;
    }

    private AtomicReference<Exception> runTwoLockingThreads(PLContext plContext) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        AtomicReference<Exception> exception = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch latch2 = new CountDownLatch(1);
        OutputGenerator<TimeoutTest.TestEntityType> lockOutputGenerator = new TimeoutTest.LockOutputGenerator(table1, latch, latch2);
        ChangeFlowConfig<TimeoutTest.TestEntityType> lockFlowConfig = ChangeFlowConfigBuilderFactory.newInstance(plContext, entityType).withoutOutputGenerators().withOutputGenerator(lockOutputGenerator).build();

        Future<?> lockTask = executorService.submit(() -> {
            persistenceLayer.update(ImmutableList.of(createUpdCmd(UPDATE_VALUE)), lockFlowConfig);
        });

        OutputGenerator<TimeoutTest.TestEntityType> timeoutOutputGenerator = new TimeoutTest.TimeoutOutputGenerator(table1, latch, latch2);
        ChangeFlowConfig<TimeoutTest.TestEntityType> timeOutflowConfig = ChangeFlowConfigBuilderFactory.newInstance(plContext, entityType).withoutOutputGenerators().withOutputGenerator(timeoutOutputGenerator).build();

        Future<?> timeOutTask = executorService.submit(() -> {
            try {
                persistenceLayer.update(ImmutableList.of(createUpdCmd(RETRY_VALUE)), timeOutflowConfig);
            } catch (Exception e) {
                exception.set(e);
            }
        });

        waitTasks(lockTask, timeOutTask);

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.SECONDS);
        return exception;
    }

    private UpdateEntityCommand<TestEntityType, TestEntityType.Key> createUpdCmd(String retryValue) {
        UpdateEntityCommand<TestEntityType, TestEntityType.Key> cmd = new UpdateEntityCommand<>(entityType, new TestEntityType.Key(ID));
        cmd.set(TestEntityType.FIELD, retryValue);
        return cmd;
    }

    private void waitTasks(Future<?> task1, Future<?> task2) {
        ImmutableList.of(task1, task2).forEach(t -> {
            try {
                t.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
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

    private class LockOutputGenerator implements OutputGenerator<TestEntityType> {

        private final TestTable table1;
        private final CountDownLatch startTimpouThread;
        private final CountDownLatch finishLockingThread;

        LockOutputGenerator(TestTable table1, CountDownLatch latch, CountDownLatch latch2) {
            this.table1 = table1;
            this.startTimpouThread = latch;
            this.finishLockingThread = latch2;
        }

        @Override
        public void generate(Collection<? extends EntityChange<TestEntityType>> entityChanges, ChangeOperation changeOperation, ChangeContext changeContext) {
            try {
                EntityChange<TestEntityType> first = Iterables.getFirst(entityChanges, null);
                dslContext.update(table1).set(table1.field, first.get(TestEntityType.FIELD)).where(table1.id.eq(first.getIdentifier().get(TestEntityType.ID))).execute();
                finishLockingThread.countDown();
                try {
                    startTimpouThread.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            } catch (Exception e) {
                throw Throwables.propagate(e);
            } finally {
                finishLockingThread.countDown();
            }
        }

        @Override
        public Stream<? extends EntityField<?, ?>> requiredFields(Collection<? extends EntityField<TestEntityType, ?>> fieldsToUpdate, ChangeOperation changeOperation) {
            return Stream.empty();
        }
    }

    private class TimeoutOutputGenerator implements OutputGenerator<TestEntityType> {

        private final TestTable table1;
        private final CountDownLatch latch;
        private final CountDownLatch latch2;

        TimeoutOutputGenerator(TestTable table1, CountDownLatch latch, CountDownLatch latch2) {
            this.table1 = table1;
            this.latch = latch;
            this.latch2 = latch2;
        }

        @Override
        public void generate(Collection<? extends EntityChange<TestEntityType>> entityChanges, ChangeOperation changeOperation, ChangeContext changeContext) {
            try {
                latch2.await();
                EntityChange<TestEntityType> first = Iterables.getFirst(entityChanges, null);
                dslContext.execute("SET SESSION innodb_lock_wait_timeout = 1");
                dslContext.update(table1).set(table1.field, first.get(TestEntityType.FIELD)).where(table1.id.eq(first.getIdentifier().get(TestEntityType.ID))).execute();
            } catch (Exception e) {
                throw Throwables.propagate(e);
            } finally {
                latch.countDown();
            }
        }

        @Override
        public Stream<? extends EntityField<?, ?>> requiredFields(Collection<? extends EntityField<TestEntityType, ?>> fieldsToUpdate, ChangeOperation changeOperation) {
            return Stream.empty();
        }
    }
}