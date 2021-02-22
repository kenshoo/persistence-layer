package com.kenshoo.jooq;

import com.google.common.collect.Lists;
import org.jooq.*;
import org.jooq.lambda.Seq;

import java.util.List;
import java.util.stream.Collectors;

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
        populate(dslContext, table, Lists.newArrayList(table.fields()), data);
    }

    public static void populateTableWithoutAutoIncFields(DSLContext dslContext, Table<Record> table, Object[][] data) {
        var fields = Seq.of(table.fields()).filter(field -> !field.getDataType().identity()).collect(Collectors.toList());
        populate(dslContext, table,fields, data);
    }

    private static void populate(DSLContext dslContext, Table<Record> table, List<Field<?>> fields, Object[][] data) {
        InsertValuesStepN<Record> insert = dslContext.insertInto(table, fields).values(new Object[fields.size()]);
        BatchBindStep batch = dslContext.batch(insert);
        for (Object[] values : data) {
            batch.bind(values);
        }
        batch.execute();
    }
}
