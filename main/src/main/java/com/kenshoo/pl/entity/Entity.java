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

        @Override
        public <T> void set(EntityField<?, T> field, T value) {
            throw new UnsupportedOperationException("cannot set values into an EMPTY (non-existing) entity");
        }
    };

    boolean containsField(EntityField<?, ?> field);

    <T> T get(EntityField<?, T> field);

    <T> void set(EntityField<?, T> field, T value);

}
