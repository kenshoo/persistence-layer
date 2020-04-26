package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.Entity;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.FieldsValueMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;


public class EntityImpl implements Entity {

    private final Map<EntityField<?, ?>, Object> fields = new HashMap<>();
    private Map<EntityType, List<FieldsValueMap>> manyByType;

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
        return manyByType == null ? emptyList() : (List) manyByType.get(type);
    }

    public <T> void set(EntityField<?, T> field, T value) {
        fields.put(field, value);
    }

    synchronized
    public <E extends EntityType<E>> void add(E entityType, List<FieldsValueMap<E>> fieldsValueMaps) {
        if (manyByType == null) {
            manyByType = new HashMap<>();
        }
        manyByType.put(entityType, (List) fieldsValueMaps);
    }

}
