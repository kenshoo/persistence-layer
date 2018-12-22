package com.kenshoo.pl.entity;

public interface PrototypedEntityField<E extends EntityType<E>, T> extends EntityField<E, T> {

    EntityFieldPrototype<T> getEntityFieldPrototype();

}
