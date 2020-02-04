package com.kenshoo.pl.entity;

import org.jooq.lambda.Seq;

import java.util.Arrays;
import java.util.stream.Stream;

public interface Identifier<E extends EntityType<E>>  extends FieldsValueMap<E> {

    UniqueKey<E> getUniqueKey();

    default Stream<Object> getValues() {
        return Seq.of(getUniqueKey().getFields()).map(this::get);
    }

    default boolean isEmpty() {
        return !getValues().findAny().isPresent();
    }

    default int size() {
        return getUniqueKey().getFields().length;
    }

    default boolean contains(Identifier<E> otherIdentifier) {
        return Arrays.asList(getUniqueKey().getFields()).containsAll(Arrays.asList(otherIdentifier.getUniqueKey().getFields()));
    }

}
