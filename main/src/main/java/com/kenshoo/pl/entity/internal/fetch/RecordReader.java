package com.kenshoo.pl.entity.internal.fetch;

import com.google.common.collect.Iterators;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.EntityImpl;
import org.jooq.Record;

import java.util.Collection;
import java.util.Iterator;

import static org.jooq.lambda.Seq.seq;

public class RecordReader {

    public static <E extends EntityType<E>, T> Identifier<E> createKey(Record record, AliasedKey<E> aliasedKey) {
        final FieldsValueMapImpl<E> fieldsValueMap = new FieldsValueMapImpl<>();
        aliasedKey.fields().forEach(aliasedField -> {
            EntityField<E, T> field = (EntityField<E, T>) aliasedField.unAliased();
            T value = field.getDbAdapter().getFromRecord(Iterators.singletonIterator(record.getValue(aliasedField.aliased().getName())));
            fieldsValueMap.set(field, value);
        });
        return new UniqueKey<E>(aliasedKey.unAliasedFields()).createValue(fieldsValueMap);
    }

    public static <E extends EntityType<E>, T> EntityImpl createEntity(Record record, Collection<? extends EntityField<?, ?>> fields) {
        EntityImpl entity = new EntityImpl();
        Iterator<Object> valuesIterator = record.intoList().iterator();
        fields.forEach(entityField-> {
            EntityField<E, T> field = (EntityField<E, T>) entityField;
            entity.set(field, field.getDbAdapter().getFromRecord(valuesIterator));
        });
        return entity;
    }

    public static <E extends EntityType<E>> FieldsValueMap<E> createFieldsValueMap(Record record, final Collection<? extends EntityField<E, ?>> fields) {
        FieldsValueMapImpl fieldsValueMap = new FieldsValueMapImpl();
        Iterator<Object> valuesIterator = record.intoList().iterator();
        seq(fields).forEach(field-> fieldsValueMap.set(field, field.getDbAdapter().getFromRecord(valuesIterator)));
        return fieldsValueMap;
    }
}
