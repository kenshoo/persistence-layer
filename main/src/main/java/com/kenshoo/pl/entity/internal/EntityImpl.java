package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.Entity;
import com.kenshoo.pl.entity.EntityField;

import java.util.HashMap;
import java.util.Map;

public class EntityImpl implements Entity {

    private final Map<EntityField<?, ?>, Object> fields = new HashMap<>();

    public boolean containsField(EntityField<?, ?> field) {
        return fields.containsKey(field);
    }

    @Override
    public <T> T get(EntityField<?, T> field) {
        //noinspection unchecked
        T value = (T) fields.get(field);
        if (value == null && !fields.containsKey(field)) {
            throw new IllegalArgumentException("Field " + field + " of entity \"" + field.getEntityType().getName() + "\" is not fetched");
        }
        return value;
    }

    public <T> void set(EntityField<?, T> field, T value) {
        fields.put(field, value);
    }
}
