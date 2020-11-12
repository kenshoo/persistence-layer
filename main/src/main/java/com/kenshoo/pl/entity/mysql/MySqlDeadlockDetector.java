package com.kenshoo.pl.entity.mysql;

import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.kenshoo.pl.entity.spi.DeadlockDetector;
import org.jooq.lambda.Seq;
import java.sql.SQLException;
import static org.jooq.lambda.Seq.seq;


public class MySqlDeadlockDetector implements DeadlockDetector {

    private String retryPatterns = "lock wait timeout exceeded;deadlock";

    @Override
    public boolean isDeadlock(Throwable e) {
        Throwable rootCause = Throwables.getRootCause(e);
        return rootCause instanceof SQLException
                && rootCause.getMessage() != null
                && deadlockStrings().anyMatch(rootCause.getMessage().toLowerCase()::contains);
    }

    private Seq<String> deadlockStrings() {
        return seq(Splitter.on(";")
                .trimResults()
                .omitEmptyStrings()
                .splitToList(retryPatterns));
    }

}
