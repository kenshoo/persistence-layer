package com.kenshoo.pl.entity;

import com.kenshoo.pl.entity.internal.MissingChildrenSupplier;
import com.kenshoo.pl.entity.spi.MutableCommand;

public interface EntityCommandExt<E extends EntityType<E>, C extends EntityCommandExt<E, C>> extends MutableCommand<E> {

    default <T> C with(EntityField<E, T> field, T newValue) {
        set(field, newValue);
        return (C) this;
    }

    @SuppressWarnings("unchecked")
    default <T> C with(final TransientProperty<T> property, final T propertyValue) {
        set(property, propertyValue);
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
