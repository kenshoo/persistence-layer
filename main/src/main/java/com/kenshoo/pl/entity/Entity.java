package com.kenshoo.pl.entity;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public interface Entity {

    Entity EMPTY = new Entity() {
        @Override
        public boolean containsField(EntityField<?, ?> field) {
            return false;
        }

        @Override
        public <T> T get(EntityField<?, T> field) {
            return null;
        }
    };

    boolean containsField(EntityField<?, ?> field);

    <T> T get(EntityField<?, T> field);

    default <E extends EntityType<E>> List<FieldsValueMap<E>> getMany(E type) {
        return Collections.emptyList();
    }

    default <E extends EntityType<E>> List<FieldsValueMap<E>> finalChildrenState(EntityChange<E> entityChange, E type) {
        return Collections.emptyList();
    }

}
