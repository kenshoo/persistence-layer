package com.kenshoo.pl.entity;

import com.kenshoo.jooq.FieldAndValues;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.lambda.Seq;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public interface Identifier<E extends EntityType<E>>  extends FieldsValueMap<E> {

    IdentifierType<E> getUniqueKey();

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

    static <E extends EntityType<E>> List<FieldAndValues<?>> groupValuesByFields(Collection<? extends Identifier<E>> ids) {
        final IdentifierType<E> uniqueKey = ids.iterator().next().getUniqueKey();
        return Stream.of(uniqueKey.getFields())
                .map(field -> collectValuesOf(field, ids))
                .collect(toList());

    }

    static <E extends EntityType<E>, T> FieldAndValues<?> collectValuesOf(EntityField<E, T> field, Collection<? extends Identifier<E>> ids) {
        EntityFieldDbAdapter<T> dbAdapter = field.getDbAdapter();
        List<Object> fieldValues = ids.stream().flatMap(id -> dbAdapter.getDbValues(id.get(field)).sequential()).collect(toList());
        return new FieldAndValues<>((TableField<Record, Object>) dbAdapter.getFirstTableField(), fieldValues);
    }


}
