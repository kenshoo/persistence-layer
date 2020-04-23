package com.kenshoo.pl.entity;

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

        @Override
        public List<Entity> get(EntityType type) {
            return Collections.emptyList();
        }
    };

    boolean containsField(EntityField<?, ?> field);

    <T> T get(EntityField<?, T> field);

    List<Entity> get(EntityType type);

}
