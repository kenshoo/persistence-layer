package com.kenshoo.pl.entity;

import java.util.Map;

public class PrototypeFieldsCombination<E extends EntityType<E>> {

    final private Map<EntityFieldPrototype<?>, EntityField<E, ?>> fieldsMapping;
    final private FieldsValueMap<E> fieldsValueMap;

    public PrototypeFieldsCombination(Map<EntityFieldPrototype<?>, EntityField<E, ?>> fieldsMapping, FieldsValueMap<E> fieldsValueMap) {
        this.fieldsMapping = fieldsMapping;
        this.fieldsValueMap = fieldsValueMap;
    }

    public <T> T get(EntityFieldPrototype<T> field) {
        //noinspection unchecked
        EntityField<E,T> entityField = (EntityField<E,T>)fieldsMapping.get(field);
        return fieldsValueMap.get(entityField);
    }
}
