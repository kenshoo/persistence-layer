package com.kenshoo.jooq;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SelectConnectByStep;
import org.jooq.UpdateConditionStep;
import org.jooq.UpdateWhereStep;

import java.util.Collection;
import java.util.Iterator;

public class IntIdsList extends IdsListImpl<Integer> implements IdsList<Integer> {

    public IntIdsList(DSLContext dslContext) {
        super(dslContext, IdsTempTable.INT_TABLE);
    }

    public IntIdsList(DSLContext dslContext, Collection<Integer> ids) {
        super(dslContext, IdsTempTable.INT_TABLE, ids);
    }

    public IdsList<Long> asLongIdsList() {
        return new AsLongIdsList();
    }

    private class AsLongIdsList implements IdsList<Long> {
        private IdsList<Integer> delegate = IntIdsList.this;

        @Override
        public void add(Long id) {
            throw new UnsupportedOperationException("Adding IDs to int list wrapper is not allowed");
        }

        @Override
        public void addAll(Collection<Long> ids) {
            throw new UnsupportedOperationException("Adding IDs to int list wrapper is not allowed");
        }

        @Override
        public boolean isEmpty() {
            return delegate.isEmpty();
        }

        @Override
        public Iterator<Long> iterator() {
            return Iterators.transform(delegate.iterator(), new Function<Integer, Long>() {
                @Override
                public Long apply(Integer input) {
                    return (long) input;
                }
            });
        }

        @Override
        public void close() {
            throw new UnsupportedOperationException("Closing int list wrapper is not allowed");
        }

        @Override
        public <R extends Record, S extends SelectConnectByStep<R>, FT> S imposeOnQuery(S query, Field<FT> idField) {
            return delegate.imposeOnQuery(query, idField);
        }

        @Override
        public <R extends Record, S extends UpdateWhereStep<R>, FT> UpdateConditionStep<R> imposeOnUpdate(S update, Field<FT> idField) {
            return delegate.imposeOnUpdate(update, idField);
        }
    }
}
