package com.kenshoo.pl.entity;

import com.github.rholder.retry.Attempt;
import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.RetryListener;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.kenshoo.pl.entity.spi.DeadlockDetector;
import com.kenshoo.pl.entity.spi.PersistenceLayerRetryer;
import com.kenshoo.pl.entity.spi.ThrowingAction;
import com.kenshoo.pl.entity.spi.TransactionDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.github.rholder.retry.StopStrategies.stopAfterAttempt;
import static com.github.rholder.retry.WaitStrategies.incrementingWait;


public class DeadlockRetryer implements PersistenceLayerRetryer {

    private final static Logger logger = LoggerFactory.getLogger(DeadlockRetryer.class);

    private int maxDeadlockRetries = 5;
    private long firstSleepBetweenRetriesMillis = 1000;
    private long incrementSleepBetweenRetriesMillis = 0;

    private final DeadlockDetector deadlockDetector;
    private final TransactionDetector transactionDetector;


    public DeadlockRetryer(DeadlockDetector deadlockDetector, TransactionDetector transactionDetector) {
        this.deadlockDetector = deadlockDetector;
        this.transactionDetector = transactionDetector;
    }

    @Override
    public void run(ThrowingAction action) {
        try {
            deadlockRetryer().call(() -> {
                action.run();
                return null;
            });
        } catch (ExecutionException e) {
            throw new RuntimeException("DatabaseDeadlockRetryer: Failed to execute deadlock retryer", e);
        } catch (RetryException e) {
            throw new RuntimeException("DatabaseDeadlockRetryer: Retry failure, number of attempts " + e.getNumberOfFailedAttempts(), e);
        }
    }

    private Retryer<Void> deadlockRetryer() {
        return RetryerBuilder.<Void>newBuilder()
                .retryIfException(e -> deadlockDetector.isDeadlock(e) && !transactionDetector.isActiveTransactionExist())
                .withStopStrategy(stopAfterAttempt(maxDeadlockRetries))
                .withWaitStrategy(incrementingWait(firstSleepBetweenRetriesMillis, TimeUnit.MILLISECONDS, incrementSleepBetweenRetriesMillis, TimeUnit.MILLISECONDS))
                .withRetryListener(new RetryListener() {
                    @Override
                    public <V> void onRetry(Attempt<V> attempt) {
                        long attemptNumber = attempt.getAttemptNumber();
                        if (attemptNumber > 1) {
                            logger.warn("DatabaseDeadlockRetryer: got deadlock when saving to database. This was try {} out of {}", attemptNumber, maxDeadlockRetries);
                            logger.warn("DatabaseDeadlockRetryer: delaySinceFirstAttempt: {}", attempt.getDelaySinceFirstAttempt());
                            if (attemptNumber >= maxDeadlockRetries) {
                                logger.error("DatabaseDeadlockRetryer: Unsuccessfully retried deadlocked transaction {} times " +
                                                "with first sleep of {} milliseconds and increment multiplier of {} milliseconds",
                                        maxDeadlockRetries, firstSleepBetweenRetriesMillis, incrementSleepBetweenRetriesMillis);
                            }
                        }
                    }
                })
                .build();
    }


    public DeadlockRetryer setMaxDeadlockRetries(int maxDeadlockRetries) {
        this.maxDeadlockRetries = maxDeadlockRetries;
        return this;
    }

    public DeadlockRetryer setFirstSleepBetweenRetriesMillis(long firstSleepBetweenRetriesMillis) {
        this.firstSleepBetweenRetriesMillis = firstSleepBetweenRetriesMillis;
        return this;
    }

    public DeadlockRetryer setIncrementSleepBetweenRetriesMillis(long incrementSleepBetweenRetriesMillis) {
        this.incrementSleepBetweenRetriesMillis = incrementSleepBetweenRetriesMillis;
        return this;
    }


}
