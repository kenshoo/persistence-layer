package com.kenshoo.jooq;

import org.jooq.BatchBindStep;
import org.jooq.CreateTableAsStep;
import org.jooq.CreateTableColumnStep;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.InsertValuesStepN;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;

public class DataTableUtils {

    public static <T extends Record> void createTable(DSLContext dslContext, Table<T> table) {
        dslContext.dropTableIfExists(table).execute();
        CreateTableAsStep<Record> createTableAsStep = dslContext.createTable(table);
        CreateTableColumnStep createTableColumnStep = null;
        for (Field<?> field : table.fields()) {
            if (createTableColumnStep != null) {
                createTableColumnStep = addColumn(createTableColumnStep, field);
            } else {
                createTableColumnStep = addColumn(createTableAsStep, field);
            }
        }
        assert createTableColumnStep != null;
        createTableColumnStep.execute();
        UniqueKey<T> primaryKey = table.getPrimaryKey();
        if (primaryKey != null){
            dslContext
                    .alterTable(table)
                    .add(DSL.constraint("").primaryKey(primaryKey.getFieldsArray()))
                    .execute();
        }
    }

    private static <T> CreateTableColumnStep addColumn(CreateTableColumnStep createStep, Field<T> field) {
        return createStep.column(field, field.getDataType());
    }

    private static <T> CreateTableColumnStep addColumn(CreateTableAsStep<?> createStep, Field<T> field) {
        return createStep.column(field, field.getDataType());
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
