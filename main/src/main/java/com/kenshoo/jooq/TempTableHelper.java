package com.kenshoo.jooq;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;

public class TempTableHelper {

    public static <T extends Table<Record>> TempTableResource<T> tempInMemoryTable(final DSLContext dslContext, T table, TablePopulator tablePopulator) {
        return TempTableEngine.tempInMemoryTable(dslContext, table, tablePopulator);
    }

    public static <T extends Table<Record>> TempTableResource<T> tempInMemoryTable(final DSLContext dslContext, T table, Field<?>[] fields, TablePopulator tablePopulator) {
        return TempTableEngine.tempTable(dslContext, table, fields, tablePopulator);
    }
}
