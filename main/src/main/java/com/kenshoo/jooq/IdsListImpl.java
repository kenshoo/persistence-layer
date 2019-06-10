package com.kenshoo.jooq;

import com.google.common.base.Preconditions;
import org.jooq.BatchBindStep;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SelectConnectByStep;
import org.jooq.SelectJoinStep;
import org.jooq.SelectWhereStep;
import org.jooq.UpdateConditionStep;
import org.jooq.UpdateWhereStep;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * An abstraction of a list of IDs designed to hide the decision whether IN clause or in-memory temporary
 * table should be used. Because of that <b>it is very important to use this class as a resource</b>,
 * in a try-with-resources statement.
 */
abstract class IdsListImpl<T> implements IdsList<T> {

    private final DSLContext dslContext;
    private final List<T> ids = new ArrayList<>();
    private final IdsTempTable<T> idsTempTable;
    private TempTableResource tempTableResource;

    public IdsListImpl(DSLContext dslContext, IdsTempTable<T> idsTempTable) {
        this.dslContext = dslContext;
        this.idsTempTable = idsTempTable;
    }

    public IdsListImpl(DSLContext dslContext, IdsTempTable<T> idsTempTable, Collection<T> ids) {
        this(dslContext, idsTempTable);
        this.ids.addAll(ids);
    }

    @Override
    public void add(T id) {
        ids.add(id);
    }

    @Override
    public void addAll(Collection<T> ids) {
        this.ids.addAll(ids);
    }

    @Override
    public boolean isEmpty() {
        return ids.isEmpty();
    }

    @Override
    public Iterator<T> iterator() {
        return ids.iterator();
    }

    @Override
    public <R extends Record, S extends SelectConnectByStep<R>, FT> S imposeOnQuery(S query, Field<FT> idField) {
        //noinspection unchecked
        return (S) _imposeOnQuery(query, idField);
    }

    private <FT> Object _imposeOnQuery(Object object, Field<FT> idField) {
        if (shouldUseTempTable()) {
            Preconditions.checkArgument(object instanceof SelectJoinStep, "Expected " + SelectJoinStep.class.getName() + " but got " + object.getClass().getName());
            populateTempTable();
            //noinspection ConstantConditions,unchecked
            SelectJoinStep<Record> query = (SelectJoinStep<Record>) object;
            // The idField parameter supports a different type from <T> for the case when we join INT temp ids with BIGINT idField
            // The cast below in this case is actually a no-op in jOOQ (SQL is not changed) but it makes the compiler happy
            return query.join(idsTempTable).on(idsTempTable.id.cast(idField.getDataType()).eq(idField));
        } else {
            Preconditions.checkArgument(object instanceof SelectWhereStep, "Expected " + SelectWhereStep.class.getName() + " but got " + object.getClass().getName());
            //noinspection unchecked,ConstantConditions
            SelectWhereStep<Record> query = (SelectWhereStep<Record>) object;
            return query.where(idField.in(ids));
        }
    }

    @Override
    public <R extends Record, S extends UpdateWhereStep<R>, FT> UpdateConditionStep<R> imposeOnUpdate(S update, Field<FT> idField) {
        return update.where(idField.in(ids));
    }

    private void populateTempTable() {
        if (tempTableResource != null) {
            return;
        }
        tempTableResource = TempTableHelper.tempInMemoryTable(dslContext, idsTempTable, new TablePopulator() {
            @Override
            public void populate(BatchBindStep batchBindStep) {
                for (T id : ids) {
                    batchBindStep.bind(id);
                }
            }
        });
    }

    @Override
    public void close() {
        if (tempTableResource != null) {
            tempTableResource.close();
        }
    }

    private boolean shouldUseTempTable() {
        return ids.size() > 10;
    }

    public List<T> getIds() {
        return ids;
    }

}
