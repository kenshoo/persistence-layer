package com.kenshoo.pl.entity;

import com.kenshoo.pl.entity.mysql.MySqlDeadlockDetector;
import com.kenshoo.pl.entity.spi.ThrowingAction;
import com.mysql.cj.jdbc.exceptions.MySQLTransactionRollbackException;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class DeadlockRetryerTest {

    private DeadlockRetryer classUnderTest;

    private ThrowingAction updateToDB = mock(ThrowingAction.class);

    @Before
    public void setUp() {
        classUnderTest = new DeadlockRetryer(new MySqlDeadlockDetector(), () -> false)
                .setFirstSleepBetweenRetriesMillis(1)
                .setIncrementSleepBetweenRetriesMillis(1)
                .setMaxDeadlockRetries(5);
    }

    @Test
    public void update_Should_succeed_in_case_of_no_exceptions() throws Exception {
        classUnderTest.run(updateToDB);
        verify(updateToDB, times(1)).run();
    }

    @Test
    public void update_should_retry_when_SQL_exception_contains_lock_wait_timeout_exceeded() throws Exception {
        doAnswer(invocation -> {
            throw new SQLException("test lock wait timeout exceeded test");
        })
                .doAnswer(invocation -> {
                    throw new SQLException("test lock wait timeout exceeded test");
                })
                .doAnswer(invocation -> {
                    throw new SQLException("test lock wait timeout exceeded test");
                })
                .doNothing()
                .when(updateToDB).run();

        classUnderTest.run(updateToDB);
        verify(updateToDB, times(4)).run();
    }

    @Test
    public void update_should_retry_when_SQL_exception_contains_word_deadlock() throws Exception {
        doAnswer(invocation -> {
            throw new MySQLTransactionRollbackException("test 111DeAdLock222 test");
        })
                .doAnswer(invocation -> {
                    throw new MySQLTransactionRollbackException("test 111DeAdLock222 test");
                })
                .doAnswer(invocation -> {
                    throw new MySQLTransactionRollbackException("test 111DeAdLock222 test");
                })
                .doNothing()
                .when(updateToDB).run();

        classUnderTest.run(updateToDB);
        verify(updateToDB, times(4)).run();
    }

    @Test
    public void update_should_not_retry_on_non_SQL_exception_types() throws Exception {
        doAnswer(invocation -> {
            throw new RuntimeException("test 111DeAdLock222 test");
        })
                .doAnswer(invocation -> {
                    throw new RuntimeException("test 111DeAdLock222 test");
                })
                .doAnswer(invocation -> {
                    throw new RuntimeException("test 111DeAdLock222 test");
                })
                .doNothing()
                .when(updateToDB).run();
        try {
            classUnderTest.run(updateToDB);
        } catch (RuntimeException e) {
            verify(updateToDB, times(1)).run();
        }
    }

    @Test
    public void update_should_not_retry_when_SQL_exception_message_is_not_recognized() throws Exception {
        doAnswer(invocation -> {
            throw new SQLException("unrecognized message");
        })
                .doAnswer(invocation -> {
                    throw new SQLException("unrecognized message");
                })
                .doAnswer(invocation -> {
                    throw new SQLException("unrecognized message");
                })
                .doNothing()
                .when(updateToDB).run();
        try {
            classUnderTest.run(updateToDB);
        } catch (RuntimeException e) {
            verify(updateToDB, times(1)).run();
        }
    }

    @Test
    public void update_should_stop_retrying_after_max_number_of_attempts() throws Exception {
        doAnswer(invocation -> {
            throw new SQLException("bla_bla_ lock wait timeout exceeded _bla_");
        })
                .doAnswer(invocation -> {
                    throw new SQLException("bla_bla_ lock wait timeout exceeded _bla_");
                })
                .doAnswer(invocation -> {
                    throw new SQLException("bla_bla_ lock wait timeout exceeded _bla_");
                })
                .doAnswer(invocation -> {
                    throw new SQLException("bla_bla_ lock wait timeout exceeded _bla_");
                })
                .doAnswer(invocation -> {
                    throw new SQLException("bla_bla_ lock wait timeout exceeded _bla_");
                })
                .doNothing()
                .when(updateToDB).run();

        try {
            classUnderTest.run(updateToDB);
        } catch (RuntimeException e) {
            verify(updateToDB, times(5)).run();
        }
    }
}