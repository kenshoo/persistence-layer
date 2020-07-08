package com.kenshoo.pl.entity;

public interface CurrentEntityState extends Entity {

    CurrentEntityState EMPTY = new CurrentEntityState() {
        @Override
        public boolean containsField(EntityField<?, ?> field) {
            return false;
        }

        @Override
        public <T> T get(EntityField<?, T> field) {
            return null;
        }
    };

}
