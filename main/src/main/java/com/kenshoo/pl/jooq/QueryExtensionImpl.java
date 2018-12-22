package com.kenshoo.pl.jooq;

import com.google.common.base.Preconditions;
import org.jooq.*;
import org.jooq.impl.DSL;

import java.util.Iterator;
import java.util.List;

import static java.util.stream.Collectors.toList;

class QueryExtensionImpl<Q extends SelectFinalStep> implements QueryExtension<Q> {

    private TempTableResource tempTableResource;

    private final Q query;

    QueryExtensionImpl(TempTableHelper tempTableHelper, Q query, final List<FieldAndValues<?>> fieldsWithValues) {
        if (shouldUseTempTable(fieldsWithValues)) {
            Preconditions.checkArgument(query instanceof SelectJoinStep, "Expected " + SelectJoinStep.class.getName() + " but got " + query.getClass().getName());
            TuplesTempTable tempTable = createTempTableDefinition(fieldsWithValues);
            // A rather hacky way to extract DSLContext out of existing query. Better than require the client to pass it though
            DSLContext dslContext = DSL.using(((AttachableInternal) query.getQuery()).configuration());
            tempTableResource = tempTableHelper.tempInMemoryTable(dslContext, tempTable, new TempTablePopulator(fieldsWithValues));
            //noinspection unchecked
            this.query = addJoinToQuery((SelectJoinStep) query, tempTable, fieldsWithValues);
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
        return fieldsWithValues.size() > 1 || fieldsWithValues.get(0).getValues().size() > 10;
    }

    private Q addJoinToQuery(SelectJoinStep joinStep, TuplesTempTable tempTable, List<FieldAndValues<?>> fieldsWithValues) {
        Condition joinCondition = DSL.trueCondition();
        for (int i = 0; i < fieldsWithValues.size(); i++) {
            FieldAndValues<?> fieldsWithValue = fieldsWithValues.get(i);
            //noinspection unchecked
            joinCondition = joinCondition.and(fieldsWithValue.getField().eq((Field) tempTable.field(i)));
        }
        //noinspection unchecked
        return (Q) joinStep.join(tempTable).on(joinCondition);
    }

    private TuplesTempTable createTempTableDefinition(List<FieldAndValues<?>> fieldsWithValues) {
        TuplesTempTable tempTable = new TuplesTempTable();
        int i = 1;
        for (FieldAndValues<?> fieldsWithValue : fieldsWithValues) {
            tempTable.addField("field" + i++, fieldsWithValue.getField().getDataType());
        }
        return tempTable;
    }

    @Override
    public Q getQuery() {
        return query;
    }

    @Override
    public void close() {
        if (tempTableResource != null) {
            try {
                tempTableResource.close();
            } catch (Exception ignore) {
            }
        }
    }

    private static class TempTablePopulator implements TablePopulator {
        private final List<FieldAndValues<?>> fieldsWithValues;

        public TempTablePopulator(List<FieldAndValues<?>> fieldsWithValues) {
            this.fieldsWithValues = fieldsWithValues;
        }

        @Override
        public void populate(BatchBindStep batchBindStep) {
            List<Iterator<?>> iterators = fieldsWithValues.stream().map(input -> input.getValues().iterator()).collect(toList());
            while (iterators.get(0).hasNext()) {
                batchBindStep.bind(iterators.stream().map(Iterator::next).toArray(Object[]::new));
            }
        }
    }
}
