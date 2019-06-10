package com.kenshoo.jooq;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SelectJoinStep;
import org.jooq.Table;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

public abstract class QueryEngine<C extends Cursor> {

    private final Table table;
    @Resource
    protected DSLContext dslContext;

    protected QueryEngine(Table table) {
        this.table = table;
    }

    public SelectJoinStep<Record> select(Field<?>... fields) {
        return dslContext.select(fields).from(table);
    }

    public void query(Field<?>[] fields, Condition condition, RecordProcessor<C> processor) {
        org.jooq.Cursor<Record> records = select(fields).where(condition).fetchSize(Integer.MIN_VALUE).fetchLazy();
        try {
            for (Record record : records) {
                processor.process(createCursor(record));
            }
        } finally {
            records.close();
        }
    }

    public <T> List<T> query(Field<?>[] fields, Condition condition, RecordMapper<C, T> mapper) {
        org.jooq.Cursor<Record> records = select(fields).where(condition).fetchSize(Integer.MIN_VALUE).fetchLazy();
        List<T> list = new ArrayList<>();
        try {
            for (Record record : records) {
                list.add(mapper.map(createCursor(record)));
            }
        } finally {
            records.close();
        }
        return list;
    }

    protected abstract C createCursor(Record record);

}
