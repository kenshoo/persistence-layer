package com.kenshoo.pl.entity;

import org.jooq.Record;
import org.jooq.TableField;

import java.util.List;

public interface IdentifierType<E extends EntityType<E>> {

    Identifier<E> createIdentifier(FieldsValueMap<E> fieldsValueMap);

    Identifier<E> createIdentifier(Entity entity);

    E getEntityType();

    EntityField<E, ?>[] getFields();

    List<TableField<Record, ?>> getTableFields();

    static <E extends EntityType<E>, T1> SingleUniqueKey<E, T1> uniqueKey(EntityField<E, T1> f1) {
        return new SingleUniqueKey<>(f1);
    }

    static <E extends EntityType<E>, T1, T2> PairUniqueKey<E, T1, T2> uniqueKey(
                                            EntityField<E, T1> f1,
                                            EntityField<E, T2> f2) {
        return new PairUniqueKey<>(f1, f2);
    }

    static <E extends EntityType<E>, T1, T2, T3> TripleUniqueKey<E, T1, T2, T3> uniqueKey(
                                            EntityField<E, T1> f1,
                                            EntityField<E, T2> f2,
                                            EntityField<E, T3> c) {
        return new TripleUniqueKey<>(f1, f2, c);
    }

    static <E extends EntityType<E>, T1, T2, T3, T4> QuadrupleUniqueKey<E, T1, T2, T3, T4> uniqueKey(
                                            EntityField<E, T1> f1,
                                            EntityField<E, T2> f2,
                                            EntityField<E, T3> f3,
                                            EntityField<E, T4> f4) {
        return new QuadrupleUniqueKey<>(f1, f2, f3, f4);
    }

    static <E extends EntityType<E>, T1, T2, T3, T4, T5> QuintupleUniqueKey<E, T1, T2, T3, T4, T5> uniqueKey(
            EntityField<E, T1> f1,
            EntityField<E, T2> f2,
            EntityField<E, T3> f3,
            EntityField<E, T4> f4,
            EntityField<E, T5> f5) {
        return new QuintupleUniqueKey<>(f1, f2, f3, f4, f5);
    }
}
