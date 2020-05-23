package com.kenshoo.jooq;

import com.google.common.collect.ImmutableList;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.impl.TableImpl;

import java.util.Collection;

/**
 * Provides a convenient (and MySQL-friendly) way of solving the use-case of deleting records by N identifiers.
 * The simplest case is deleting by a list of IDs in which case it could be expressed with a simple IN. However in
 * terms of performance (and logs) it's better to do a join with a temporary table populated with those IDs instead.
 * This class does it automatically.
 *
 * <b>The returned object is a resource and it is crucial to place it inside try/finally block so it could be closed</b>
 */
public class DeleteQueryBuilder {

    private final DSLContext dslContext;

    public DeleteQueryBuilder(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public BuilderWith table(final TableImpl<Record> table) {

        return new BuilderWith() {
            @Override
            public <T> BuilderIn1<T> withCondition(final Field<T> field) {
                return values -> new DeleteQueryExtension(table, ImmutableList.of(
                        new FieldAndValues<>(field, values)),
                        dslContext);
            }
        };
    }


    public interface BuilderWith {
        <T> BuilderIn1 withCondition(Field<T> field);
    }


    public interface BuilderIn1<T> {
        DeleteQueryExtension in(Collection<T> values);
    }
}
