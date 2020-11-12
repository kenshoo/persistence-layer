package com.kenshoo.jooq;


import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.impl.TableImpl;

import java.util.List;

import static com.kenshoo.jooq.QueryExtension.JOIN_TEMP_TABLE_LIMIT;
import static org.jooq.lambda.Seq.seq;

public class DeleteQueryExtension implements AutoCloseable {

    private Query query;
    private TempTableCreator tempTableCreator;

    DeleteQueryExtension(final TableImpl<Record> table,
                         final List<FieldAndValues<?>> fieldWithValues,
                         final DSLContext dslContext) {

        tempTableCreator = new TempTableCreator(dslContext, fieldWithValues);

        String query = "";

        if (shouldUseTempTable(fieldWithValues)) {
            query = "DELETE " + table.getName() +
                    " FROM " + table.getName() +
                    buildJoinToTempTableQuery(fieldWithValues);
        } else {
            query = "DELETE FROM " + table.getName() +
                    buildSimpleWhereInQuery(fieldWithValues);
        }

        this.query = dslContext.query(query);
    }

    private String buildJoinToTempTableQuery(final List<FieldAndValues<?>> fieldWithValues) {
        return tempTableCreator.getJoinToTempTableClause(fieldWithValues);
    }

    private String buildSimpleWhereInQuery(final List<FieldAndValues<?>> fieldWithValues) {
        FieldAndValues<?> fieldAndValues = fieldWithValues.get(0);
        return " WHERE " + fieldAndValues.getField().getName() +
                " IN (" + seq(fieldAndValues.getValues()).toString(",") + ")";
    }

    private boolean shouldUseTempTable(List<FieldAndValues<?>> fieldsWithValues) {
        return fieldsWithValues.size() > 1 || fieldsWithValues.get(0).getValues().size() > JOIN_TEMP_TABLE_LIMIT;
    }

    public Query getQuery() {
        return query;
    }

    @Override
    public void close() {
        tempTableCreator.close();
    }
}
