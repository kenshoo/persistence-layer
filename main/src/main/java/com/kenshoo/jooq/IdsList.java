package com.kenshoo.jooq;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SelectConnectByStep;
import org.jooq.UpdateConditionStep;
import org.jooq.UpdateWhereStep;

import java.util.Collection;
import java.util.Iterator;

/**
 * An abstraction of a list of IDs designed to hide the decision whether IN clause or in-memory temporary
 * table should be used. Because of that <b>it is very important to use this class as a resource</b>,
 * in a try-with-resources statement.
 */
public interface IdsList<T> extends AutoCloseable, Iterable<T> {

    void add(T id);

    void addAll(Collection<T> ids);

    boolean isEmpty();

    @Override
    Iterator<T> iterator();

    @Override
    void close();

    <R extends Record, S extends SelectConnectByStep<R>> S imposeOnQuery(S query, Field<T> idField);

    <R extends Record, S extends UpdateWhereStep<R>> UpdateConditionStep<R> imposeOnUpdate(S update, Field<T> idField);
}
