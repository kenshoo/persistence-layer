package com.kenshoo.pl.entity.internal.fetch;

import com.google.common.collect.Iterators;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.EntityImpl;
import org.jooq.Record;
import org.jooq.lambda.Seq;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

public class RecordReader {

    public static <E extends EntityType<E>, T> Identifier<E> createKey(Record record, UniqueKey<E> uniqueKey, Optional<String> prefixAlias) {
        final FieldsValueMapImpl<E> fieldsValueMap = new FieldsValueMapImpl<>();
        Seq.of(uniqueKey.getFields()).forEach(keyField -> {
            EntityField<E, T> field = (EntityField<E, T>) keyField;
            T value = field.getDbAdapter().getFromRecord(Iterators.singletonIterator(record.getValue(prefixAlias.orElse("") + field.getDbAdapter().getFirstTableField().getName())));
            fieldsValueMap.set(field, value);
        });
        return uniqueKey.createValue(fieldsValueMap);
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
}
