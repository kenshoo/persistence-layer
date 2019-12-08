package com.kenshoo.pl.entity;

import com.kenshoo.pl.entity.internal.MissingChildrenSupplier;

import java.util.Optional;

public class DeletionOfOther<E extends EntityType<E>> implements MissingChildrenSupplier<E> {

    private final E childType;

    public DeletionOfOther(E childType) {
        this.childType = childType;
    }

    public Optional<ChangeEntityCommand<E>> supplyNewCommand(Identifier<E> id) {
        return Optional.of(new DeleteEntityCommand(childType, id));
    }

    @Override
    public E getChildType() {
        return childType;
    }
}
