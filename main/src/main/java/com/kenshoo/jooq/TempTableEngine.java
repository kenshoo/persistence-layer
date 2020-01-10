package com.kenshoo.jooq;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import org.jooq.*;
import org.jooq.impl.DSL;

import java.sql.SQLException;
import java.util.Objects;
import java.util.stream.Stream;


class TempTableEngine {

    static public <T extends Table<Record>> TempTableResource<T> tempInMemoryTable(final DSLContext dslContext, T table, TablePopulator tablePopulator) {
        return tempTable(dslContext, table, table.fields(), tablePopulator);
    }

    static public <T extends Table<Record>> TempTableResource<T> tempTable(final DSLContext dslContext, T table, Field<?>[] fields, TablePopulator tablePopulator) {
        Preconditions.checkArgument(fields.length > 0, "At least one field is required");
        // Try creating the temp table either in memory or on disk and take the first one that succeeds
        return Stream.of(TempTable.Type.IN_MEMORY, TempTable.Type.REGULAR)
                .map(type -> tempTable(dslContext, table, fields, tablePopulator, type))
                .filter(Objects::nonNull)
                .findFirst().orElseThrow(() -> new RuntimeException("Failed to create temp table"));
    }

    static private <T extends Table<Record>> TempTableResource<T> tempTable(final DSLContext dslContext, T table, Field<?>[] fields, TablePopulator tablePopulator, TempTable.Type tableType) {
        final TransactionProvider txProvider = dslContext.configuration().transactionProvider();
        final Configuration newJooq = dslContext.configuration().derive();
        final TransactionContext tx = new TransactionContextImpl(newJooq, dslContext);
        txProvider.begin(tx);
        TempTable<T> tempTable = new TempTable<>(DSL.using(newJooq), table, fields, tablePopulator, tableType);
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
                        txProvider.commit(tx);
                    }
                }
            };
        } catch (Throwable e) {
            txProvider.rollback(tx);
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
