package com.kenshoo.jooq;

import org.jooq.*;

import static org.jooq.impl.DSL.constraint;

public class DataTableUtils {

    public static <T extends Record> void createTable(final DSLContext dslContext, final Table<T> table) {
        dslContext.dropTableIfExists(table).execute();

        final CreateTableColumnStep createTableAsStep = dslContext.createTable(table)
                                                                  .columns(table.fields());

        final UniqueKey<T> primaryKey = table.getPrimaryKey();
        if (primaryKey != null) {
            createTableAsStep.constraints(constraint("").primaryKey(primaryKey.getFieldsArray()))
                             .execute();
        } else {
            createTableAsStep.execute();
        }
    }

    public static void populateTable(DSLContext dslContext, Table<Record> table, Object[][] data) {
        InsertValuesStepN<Record> insert = dslContext.insertInto(table, table.fields()).values(new Object[table.fields().length]);
        BatchBindStep batch = dslContext.batch(insert);
        for (Object[] values : data) {
            batch.bind(values);
        }
        batch.execute();
    }
}
