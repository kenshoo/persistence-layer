package com.kenshoo.pl.entity.internal.fetch;

import com.google.common.collect.Iterators;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.CurrentEntityState;
import org.jooq.Record;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static org.jooq.lambda.Seq.seq;

public class RecordReader {

    public static <E extends EntityType<E>> Identifier<E> createKey(Record record, AliasedKey<E> aliasedKey) {
        final FieldsValueMapImpl<E> fieldsValueMap = new FieldsValueMapImpl<>();
        aliasedKey.fields().forEach(aliasedField -> populateMap(aliasedField.unAliased(), aliasedField.aliased().getName(), record, fieldsValueMap));
        return new UniqueKey<>(aliasedKey.unAliasedFields()).createIdentifier(fieldsValueMap);
    }

    public static CurrentEntityState createEntity(Record record, Collection<? extends EntityField<?, ?>> fields) {
        CurrentEntityMutableState currentState = new CurrentEntityMutableState();
        Object[] values = convertToValueObjects(record);
        Iterator<Object> valuesIterator = Arrays.asList(values).iterator();
        fields.forEach(field -> populateEntity(field, valuesIterator, currentState));
        return currentState;
    }


    public static <E extends EntityType<E>> FieldsValueMap<E> createFieldsValueMap(Record record, List<? extends EntityField<E, ?>> fields) {
        FieldsValueMapImpl<E> fieldsValueMap = new FieldsValueMapImpl<>();
        Object[] values = convertToValueObjects(record);
        Iterator<Object> valuesIterator = Arrays.asList(values).iterator();
        seq(fields).forEach(field -> populateMap(field, valuesIterator, fieldsValueMap));
        return fieldsValueMap;
    }

    private static Object[] convertToValueObjects(Record record) {
        Object[] values = new Object[record.size()];
        for (int i = 0; i < record.size(); i++) {
            values[i] = record.get(i);
        }
        return values;
    }

    private static <E extends EntityType<E>, T> void populateMap(EntityField<E, T> field, String aliasName, Record record, FieldsValueMapImpl<E> fieldsValueMap) {
        T value = field.getDbAdapter().getFromRecord(Iterators.singletonIterator(record.getValue(aliasName)));
        fieldsValueMap.set(field, value);
    }

    private static <E extends EntityType<E>, T> void populateEntity(EntityField<E, T> entityField, Iterator<Object> valuesIterator, CurrentEntityMutableState currentState) {
         currentState.set(entityField, entityField.getDbAdapter().getFromRecord(valuesIterator));
    }

    private static <E extends EntityType<E>, T> void populateMap(EntityField<E, T> field, Iterator<Object> valuesIterator, FieldsValueMapImpl<E> fieldsValueMap) {
        fieldsValueMap.set(field, field.getDbAdapter().getFromRecord(valuesIterator));
    }
}
