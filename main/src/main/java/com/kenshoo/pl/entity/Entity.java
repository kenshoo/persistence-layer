package com.kenshoo.pl.entity;

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

}
