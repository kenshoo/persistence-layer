package com.kenshoo.pl.jooq;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;

@Component
public class TempTableHelper {

    @Resource
    private TempTableEngine tempTableEngine;

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
    public <T extends Table<Record>> TempTableResource<T> tempInMemoryTable(T table, TablePopulator tablePopulator) {
        return tempTableEngine.tempInMemoryTable(table, tablePopulator);
    }

    /**
     * @deprecated Use {@link #tempInMemoryTable(Table, TablePopulator)}
     */
    @Deprecated
    public <T extends Table<Record>> TempTableResource<T> tempInMemoryTable(DSLContext dslContext, T table, TablePopulator tablePopulator) {
        return tempTableEngine.tempInMemoryTable(table, tablePopulator);
    }

    /**
     * @deprecated Use {@link #tempInMemoryTable(Table, TablePopulator)} instead
     */
    @Deprecated
    public <T extends Table<Record>> TempTableResource<T> tempInMemoryTable(DSLContext dslContext, T table, Field<?>[] fields, TablePopulator tablePopulator) {
        return tempTableEngine.tempTable(table, fields, tablePopulator);
    }
}
