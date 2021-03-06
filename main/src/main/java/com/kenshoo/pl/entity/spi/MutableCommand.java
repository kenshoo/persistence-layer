package com.kenshoo.pl.entity.spi;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.MissingChildrenSupplier;
;import java.util.Collection;
import java.util.List;

public interface MutableCommand<E extends EntityType<E>> extends EntityChange<E> {

     <T> void set(EntityField<E, T> field, T newValue);

     <T> void set(EntityFieldPrototype<T> fieldPrototype, T newValue);

     <T> void set(EntityField<E, T> field, FieldValueSupplier<T> valueSupplier);

     <T> void set(EntityFieldPrototype<T> fieldPrototype, FieldValueSupplier<T> valueSupplier);

     void set(Collection<EntityField<E, ?>> fields, MultiFieldValueSupplier<E> valueSupplier);

     <CHILD extends EntityType<CHILD>> void addChild(ChangeEntityCommand<CHILD> childCmd);

     void add(MissingChildrenSupplier<? extends EntityType> missingChildrenSupplier);
}
