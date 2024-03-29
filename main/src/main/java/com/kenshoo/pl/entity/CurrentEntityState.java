package com.kenshoo.pl.entity;

/**
 * The current state of an entity in the database.
 * This is the state of the entity before any changes are applied and contains the current values of the entity's fields
 * which are requested by consumers like validators, enrichers and etc.
 *
 */
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
