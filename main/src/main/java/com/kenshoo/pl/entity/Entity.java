package com.kenshoo.pl.entity;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;

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

    default <T> Optional<T> getOptional(final EntityField<?, T> field) {
        if (containsField(field)) {
            return Optional.ofNullable(get(field));
        }
        return Optional.empty();
    }

    default <E extends EntityType<E>> List<FieldsValueMap<E>> getMany(E type) {
        return emptyList();
    }

    default <E extends EntityType<E>> List<FieldsValueMap<E>> finalChildrenState(EntityChange<E> entityChange, E type) {
        return emptyList();
    }

}
