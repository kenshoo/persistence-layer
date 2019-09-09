package com.kenshoo.pl.auto.inc;

import com.kenshoo.pl.entity.ChangeEntityCommand;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.spi.MutableCommand;

public interface EntityCommandExt<E extends EntityType<E>, C extends EntityCommandExt<E, C>> extends MutableCommand<E> {

    default <T> C with(EntityField<E, T> field, T newValue) {
        set(field, newValue);
        return (C) this;
    }

    default <CHILD extends EntityType<CHILD>> C with(ChangeEntityCommand<CHILD> childCmd) {
        addChild(childCmd);
        return (C) this;
    }

}
