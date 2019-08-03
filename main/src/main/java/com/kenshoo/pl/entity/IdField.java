package com.kenshoo.pl.entity;

import com.kenshoo.pl.entity.annotation.IdGeneration;

public class IdField<E extends EntityType<E>> {

    private final EntityField<E, ? extends Number> field;
    private final IdGeneration idGeneration;

    public IdField(EntityField<E, ? extends Number> field, IdGeneration idGeneration) {
        this.field = field;
        this.idGeneration = idGeneration;
    }

    public EntityField<E, ? extends Number> getField() {
        return field;
    }

    public IdGeneration getIdGeneration() {
        return idGeneration;
    }
}
