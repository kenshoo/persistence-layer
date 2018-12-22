package com.kenshoo.pl.jooq;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.Objects;
import java.util.stream.Stream;

@Component
class TempTableEngine {

    @Resource
    private PlatformTransactionManager transactionManager;
    @Resource
    private DSLContext dslContext;

    public <T extends Table<Record>> TempTableResource<T> tempInMemoryTable(T table, TablePopulator tablePopulator) {
        return tempTable(table, table.fields(), tablePopulator);
    }

    public <T extends Table<Record>> TempTableResource<T> tempTable(T table, Field<?>[] fields, TablePopulator tablePopulator) {
        Preconditions.checkArgument(fields.length > 0, "At least one field is required");
        // Try creating the temp table either in memory or on disk and take the first one that succeeds
        return Stream.of(TempTable.Type.IN_MEMORY, TempTable.Type.REGULAR)
                .map(type -> tempTable(table, fields, tablePopulator, type))
                .filter(Objects::nonNull)
                .findFirst().orElseThrow(() -> new RuntimeException("Failed to create temp table"));
    }

    private <T extends Table<Record>> TempTableResource<T> tempTable(T table, Field<?>[] fields, TablePopulator tablePopulator, TempTable.Type tableType) {
        DefaultTransactionDefinition txDef = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(txDef);
        TempTable<T> tempTable = new TempTable<>(dslContext, table, fields, tablePopulator, tableType);
        try {
            tempTable.create();
            return new TempTableResource<T>() {
                @Override
                public T getTable() {
                    return table;
                }

                @Override
                public void close() {
                    try {
                        tempTable.dropTable();
                    } finally {
                        transactionManager.commit(status);
                    }
                }
            };
        } catch (Throwable e) {
            transactionManager.rollback(status);
            if (tableType == TempTable.Type.IN_MEMORY && isFullTableError(e)) {
                return null;
            } else {
                throw Throwables.propagate(e);
            }
        }
    }

    private static boolean isFullTableError(Throwable e) {
        Throwable cause = e.getCause();
        if (cause == null) {
            return false;
        }
        if (!(cause instanceof SQLException)) {
            return false;
        }
        SQLException sqle = (SQLException) cause;
        return sqle.getErrorCode() == 1114 && "HY000".equals(sqle.getSQLState());
    }

}
