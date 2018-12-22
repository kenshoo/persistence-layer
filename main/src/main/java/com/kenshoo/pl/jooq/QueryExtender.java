package com.kenshoo.pl.jooq;

import com.google.common.collect.ImmutableList;
import org.jooq.Field;
import org.jooq.SelectFinalStep;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.jooq.lambda.tuple.Tuple5;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Provides a convenient (and MySQL-friendly) way of solving the use-case of querying records by N-tuples of identifiers.
 * The simplest case is querying by a list of IDs in which case it could be expressed with a simple IN. However in
 * terms of performance (and logs) it's better to do a join with a temporary table populated with those IDs instead.
 * This class does it automatically. In the more complicated case where the lookup is done by two fields (e.g. profile/affcode),
 * it is impossible to express it with an IN condition and a temp table has to be used in this case.
 * <p/>
 * Example of simple usage:
 * <pre>
 * AdCriterias ac = AdCriterias.TABLE;
 * SelectConditionStep&lt;Record2&lt;String, Integer&gt;&gt; query = dslContext.select(ac.affcode, ac.criteria_id)
 *         .from(ac).where(ac.profile_id.eq(profileId));
 * try (QueryExtension&lt;SelectConditionStep&lt;Record2&lt;String, Integer&gt;&gt;&gt; queryExtension =
 *         QueryExtender.of(query).withCondition(ac.affcode).in(affcodes)) {
 *     return queryExtension.getQuery().fetchMap(ac.affcode, ac.criteria_id);
 * }
 * </pre>
 * <b>The returned object is a resource and it is crucial to place it inside try/finally block so it could be closed</b>
 */
@Component
public class QueryExtender {

    @Resource
    private TempTableHelper tempTableHelper;

    public <Q extends SelectFinalStep> QueryExtension<Q> of(Q query, List<FieldAndValues<?>> conditions) {
        return new QueryExtensionImpl<>(tempTableHelper, query, conditions);
    }

    public <Q extends SelectFinalStep> BuilderWith<Q> of(final Q query) {
        return new BuilderWith<Q>() {
            @Override
            public <T> BuilderIn1<Q, T> withCondition(final Field<T> field) {
                return values -> new QueryExtensionImpl<>(tempTableHelper, query, ImmutableList.of(new FieldAndValues<>(field, values)));
            }

            @Override
            public <T1, T2> BuilderIn2<Q, T1, T2> withCondition(final Field<T1> field1, final Field<T2> field2) {
                return values -> new QueryExtensionImpl<>(tempTableHelper, query, ImmutableList.of(
                        new FieldAndValues<>(field1, values.stream().map(Tuple2::v1).collect(toList())),
                        new FieldAndValues<>(field2, values.stream().map(Tuple2::v2).collect(toList()))
                ));
            }

            @Override
            public <T1, T2, T3> BuilderIn3<Q, T1, T2, T3> withCondition(final Field<T1> field1, final Field<T2> field2, final Field<T3> field3) {
                return values -> new QueryExtensionImpl<>(tempTableHelper, query, ImmutableList.of(
                        new FieldAndValues<>(field1, values.stream().map(Tuple3::v1).collect(toList())),
                        new FieldAndValues<>(field2, values.stream().map(Tuple3::v2).collect(toList())),
                        new FieldAndValues<>(field3, values.stream().map(Tuple3::v3).collect(toList()))
                ));
            }

            @Override
            public <T1, T2, T3, T4> BuilderIn4<Q, T1, T2, T3, T4> withCondition(final Field<T1> field1, final Field<T2> field2, final Field<T3> field3, final Field<T4> field4) {
                return values -> new QueryExtensionImpl<>(tempTableHelper, query, ImmutableList.of(
                        new FieldAndValues<>(field1, values.stream().map(Tuple4::v1).collect(toList())),
                        new FieldAndValues<>(field2, values.stream().map(Tuple4::v2).collect(toList())),
                        new FieldAndValues<>(field3, values.stream().map(Tuple4::v3).collect(toList())),
                        new FieldAndValues<>(field4, values.stream().map(Tuple4::v4).collect(toList()))
                ));
            }

            @Override
            public <T1, T2, T3, T4, T5> BuilderIn5<Q, T1, T2, T3, T4, T5> withCondition(final Field<T1> field1, final Field<T2> field2, final Field<T3> field3, final Field<T4> field4, final Field<T5> field5) {
                return values -> new QueryExtensionImpl<>(tempTableHelper, query, ImmutableList.of(
                        new FieldAndValues<>(field1, values.stream().map(Tuple5::v1).collect(toList())),
                        new FieldAndValues<>(field2, values.stream().map(Tuple5::v2).collect(toList())),
                        new FieldAndValues<>(field3, values.stream().map(Tuple5::v3).collect(toList())),
                        new FieldAndValues<>(field4, values.stream().map(Tuple5::v4).collect(toList())),
                        new FieldAndValues<>(field5, values.stream().map(Tuple5::v5).collect(toList()))
                        ));
            }
        };
    }

    public interface BuilderWith<Q extends SelectFinalStep> {
        <T> BuilderIn1<Q, T> withCondition(Field<T> field);

        <T1, T2> BuilderIn2<Q, T1, T2> withCondition(Field<T1> field1, Field<T2> field2);

        <T1, T2, T3> BuilderIn3<Q, T1, T2, T3> withCondition(Field<T1> field1, Field<T2> field2, Field<T3> field3);

        <T1, T2, T3, T4> BuilderIn4<Q, T1, T2, T3, T4> withCondition(Field<T1> field1, Field<T2> field2, Field<T3> field3, Field<T4> field4);

        <T1, T2, T3, T4, T5> BuilderIn5<Q, T1, T2, T3, T4, T5> withCondition(Field<T1> field1, Field<T2> field2, Field<T3> field3, Field<T4> field4, Field<T5> field5);
    }

    public interface BuilderIn1<Q extends SelectFinalStep, T> {
        QueryExtension<Q> in(Collection<T> values);
    }

    public interface BuilderIn2<Q extends SelectFinalStep, T1, T2> {
        QueryExtension<Q> in(Collection<Tuple2<T1, T2>> values);
    }

    public interface BuilderIn3<Q extends SelectFinalStep, T1, T2, T3> {
        QueryExtension<Q> in(Collection<Tuple3<T1, T2, T3>> values);
    }

    public interface BuilderIn4<Q extends SelectFinalStep, T1, T2, T3, T4> {
        QueryExtension<Q> in(Collection<Tuple4<T1, T2, T3, T4>> values);
    }

    public interface BuilderIn5<Q extends SelectFinalStep, T1, T2, T3, T4, T5> {
        QueryExtension<Q> in(Collection<Tuple5<T1, T2, T3, T4, T5>> values);
    }
}
