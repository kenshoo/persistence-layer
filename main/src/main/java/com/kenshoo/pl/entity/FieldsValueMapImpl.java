package com.kenshoo.pl.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yuvalr on 2/14/16.
 */
public class FieldsValueMapImpl<E extends EntityType<E>> implements FieldsValueMap<E> {

    private final Map<EntityField<E, ?>, Object> values = new HashMap<>();

    public <T> void set(EntityField<E, T> field, T value) {
        values.put(field, value);
    }

    @Override
    public <T> boolean containsField(EntityField<E, T> field) {
        return values.containsKey(field);
    }

    @Override
    public <T> T get(EntityField<E, T> field) {
        //noinspection unchecked,SuspiciousMethodCalls
        return (T) values.get(field);
    }
}
