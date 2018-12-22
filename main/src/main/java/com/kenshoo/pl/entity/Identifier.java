package com.kenshoo.pl.entity;

import org.jooq.lambda.Seq;

import java.util.stream.Stream;

public interface Identifier<E extends EntityType<E>>  extends FieldsValueMap<E> {

    UniqueKey<E> getUniqueKey();

    default Stream<Object> getValues() {
        return Seq.of(getUniqueKey().getFields()).map(this::get);
    }

}
