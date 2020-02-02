package com.kenshoo.pl.entity;

import com.kenshoo.pl.entity.internal.MissingChildrenSupplier;
import com.kenshoo.pl.entity.spi.MutableCommand;

import java.util.List;

public interface EntityCommandExt<E extends EntityType<E>, C extends EntityCommandExt<E, C>> extends MutableCommand<E> {

    default <T> C with(EntityField<E, T> field, T newValue) {
        set(field, newValue);
        return (C) this;
    }

    default <CHILD extends EntityType<CHILD>> C with(ChangeEntityCommand<CHILD> childCmd) {
        addChild(childCmd);
        return (C) this;
    }

    default C with(MissingChildrenSupplier<? extends EntityType> missingChildrenSupplier) {
        add(missingChildrenSupplier);
        return (C) this;
    }

}
