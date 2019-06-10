package com.kenshoo.jooq;

import com.google.common.base.Preconditions;
import org.jooq.DSLContext;
import org.jooq.SelectFinalStep;
import org.jooq.SelectJoinStep;
import org.jooq.SelectWhereStep;

import java.util.List;

class QueryExtensionImpl<Q extends SelectFinalStep> implements QueryExtension<Q> {

    private final Q query;
    private TempTableCreator tempTableCreator;

    QueryExtensionImpl(final DSLContext dslContext,
                       final Q query,
                       final List<FieldAndValues<?>> fieldsWithValues) {

        tempTableCreator = new TempTableCreator(dslContext, fieldsWithValues);

        if (shouldUseTempTable(fieldsWithValues)) {
            Preconditions.checkArgument(query instanceof SelectJoinStep, "Expected " + SelectJoinStep.class.getName() + " but got " + query.getClass().getName());
            //noinspection unchecked
            this.query = (Q) tempTableCreator.getJoinToTempTableClause((SelectJoinStep) query, fieldsWithValues);

        } else {
            Preconditions.checkArgument(query instanceof SelectWhereStep, "Expected " + SelectWhereStep.class.getName() + " but got " + query.getClass().getName());
            //noinspection unchecked
            FieldAndValues<?> firstFieldAndValues = fieldsWithValues.get(0);
            this.query = addWhereToQuery((SelectWhereStep) query, firstFieldAndValues);
        }
    }


    private Q addWhereToQuery(SelectWhereStep query, FieldAndValues<?> firstFieldAndValues) {
        //noinspection unchecked
        return (Q) query.where(firstFieldAndValues.getField().in(firstFieldAndValues.getValues()));
    }

    private boolean shouldUseTempTable(List<FieldAndValues<?>> fieldsWithValues) {
        return fieldsWithValues.size() > 1 || fieldsWithValues.get(0).getValues().size() > JOIN_TEMP_TABLE_LIMIT;
    }

    @Override
    public Q getQuery() {
        return query;
    }

    @Override
    public void close() {
        tempTableCreator.close();
    }
}
