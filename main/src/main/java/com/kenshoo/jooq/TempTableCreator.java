package com.kenshoo.jooq;

import org.jooq.BatchBindStep;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.SelectJoinStep;
import org.jooq.SelectOnConditionStep;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple2;

import java.util.Iterator;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.jooq.lambda.Seq.seq;


class TempTableCreator {

    private final TempTableResource tempTableResource;
    private final TuplesTempTable tempTable;

    TempTableCreator(final DSLContext dslContext, final List<FieldAndValues<?>> fieldsWithValues) {
        tempTable = createTempTableDefinition(fieldsWithValues);
        tempTableResource = TempTableHelper.tempInMemoryTable(dslContext, tempTable, new TempTablePopulator(fieldsWithValues));
    }

    public void close() {
        if (tempTableResource != null) {
            try {
                tempTableResource.close();
            } catch (Exception ignore) {
            }
        }
    }

    public SelectOnConditionStep getJoinToTempTableClause(SelectJoinStep joinStep, final List<FieldAndValues<?>> fieldsWithValues) {
        final Condition joinCondition = getJoinToTempTableCondition(fieldsWithValues);
        //noinspection unchecked
        return joinStep
                .join(tempTable)
                .on(joinCondition);
    }

    public String getJoinToTempTableClause(final List<FieldAndValues<?>> fieldsWithValues) {
        Condition joinCondition = getJoinToTempTableCondition(fieldsWithValues);
        return " JOIN " + tempTable.getName() + " ON " + joinCondition.toString().replace("\"","");
    }

    private Condition getJoinToTempTableCondition(List<FieldAndValues<?>> fieldsWithValues) {
        //noinspection unchecked
        return seq(fieldsWithValues)
                .zipWithIndex()
                .map(fieldWithIndex -> getField(fieldWithIndex).eq(getFieldFromTempTable(fieldWithIndex)))
                .reduce(Condition::and)
                .orElse(DSL.trueCondition());
    }

    private Field getField(Tuple2<FieldAndValues<?>, Long> x) {
        return x.v1.getField();
    }

    private Field getFieldFromTempTable(Tuple2<FieldAndValues<?>, Long> fieldAndIndex) {
        return tempTable.field(fieldAndIndex.v2.intValue());
    }

    private TuplesTempTable createTempTableDefinition(List<FieldAndValues<?>> fieldsWithValues) {
        final TuplesTempTable tempTable = new TuplesTempTable();
        for (FieldAndValues<?> fieldsWithValue : fieldsWithValues) {
            tempTable.addField(fieldsWithValue.getField().getName(), fieldsWithValue.getField().getDataType());
        }
        return tempTable;
    }

    private static class TempTablePopulator implements TablePopulator {
        private final List<FieldAndValues<?>> fieldsWithValues;

        TempTablePopulator(List<FieldAndValues<?>> fieldsWithValues) {
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
