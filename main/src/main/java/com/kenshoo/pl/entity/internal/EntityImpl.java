package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.jooq.lambda.Seq.seq;

public class EntityImpl implements Entity {

    private final Map<EntityField<?, ?>, Object> fields = new HashMap<>();
    private Map<EntityType, FieldsValueMaps> manyByType;

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

    @Override
    public <E extends EntityType<E>> List<FieldsValueMap<E>> getMany(E type) {
        if (isNull(manyByType)) {
            return Collections.emptyList();
        }
        FieldsValueMaps<E> fieldsValueMaps = manyByType.get(type);
        return fieldsValueMaps.fieldsValueMaps;
    }

    public <T> void set(EntityField<?, T> field, T value) {
        fields.put(field, value);
    }

    public <E extends EntityType<E>> void add(E entityType, List<FieldsValueMap<E>> fieldsValueMaps) {
        if (isNull(manyByType)) {
            manyByType = new HashMap<>();
        }
        manyByType.put(entityType, new FieldsValueMaps<E>(fieldsValueMaps));
    }

    private boolean isNull(Map<EntityType, FieldsValueMaps> maps) {
        return maps == null;
    }

    private class FieldsValueMaps<E extends EntityType<E>> {
        private final List<FieldsValueMap<E>> fieldsValueMaps;

        FieldsValueMaps(List<FieldsValueMap<E>> fieldsValueMaps) {
            this.fieldsValueMaps = fieldsValueMaps;
        }
    }
}
