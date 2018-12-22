package com.kenshoo.pl.jooq;

import com.kenshoo.util.KBeansSingletonAccessor;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;

public class TempTableHelper {

    private static final KBeansSingletonAccessor<TempTableEngine> tempTableEngineAccessor = new KBeansSingletonAccessor<>("tempTableEngine");

    /**
     * To be used in try-with-resources context as
     * <pre>
     *     try (TempTable ignored = TempTableHelper.tempTable(dslContext, TEMP_TABLE, new TablePopulator() {
     *         public void populate(BatchBindStep batchBindStep) {
     *             // ...
     *         }
     *     }) {
     *         // queries involving the temp table
     *     }
     * </pre>
     * <b>Pay attention that the order of arguments to <code>batchBindStep.bind</code> in the implementation of <code>populate</code>
     * should match the order of fields passed to this method.</b>
     */
    public static <T extends Table<Record>> TempTableResource<T> tempInMemoryTable(T table, TablePopulator tablePopulator) {
        return tempTableEngineAccessor.get().tempInMemoryTable(table, tablePopulator);
    }

    /**
     * @deprecated Use {@link #tempInMemoryTable(Table, TablePopulator)}
     */
    @Deprecated
    public static <T extends Table<Record>> TempTableResource<T> tempInMemoryTable(DSLContext dslContext, T table, TablePopulator tablePopulator) {
        return tempTableEngineAccessor.get().tempInMemoryTable(table, tablePopulator);
    }

    /**
     * @deprecated Use {@link #tempInMemoryTable(Table, TablePopulator)} instead
     */
    @Deprecated
    public static <T extends Table<Record>> TempTableResource<T> tempInMemoryTable(DSLContext dslContext, T table, Field<?>[] fields, TablePopulator tablePopulator) {
        return tempTableEngineAccessor.get().tempTable(table, fields, tablePopulator);
    }
}
