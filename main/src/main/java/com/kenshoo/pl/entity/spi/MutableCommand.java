package com.kenshoo.pl.entity.spi;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.MissingChildrenSupplier;

import java.util.Collection;

public interface MutableCommand<E extends EntityType<E>> extends EntityChange<E> {

     <T> void set(EntityField<E, T> field, T newValue);

     <T> void set(EntityFieldPrototype<T> fieldPrototype, T newValue);

     <T> void set(EntityField<E, T> field, FieldValueSupplier<T> valueSupplier);

     <T> void set(EntityFieldPrototype<T> fieldPrototype, FieldValueSupplier<T> valueSupplier);

     /**
      * Add or replace the value of a given transient property in this command.<br>
      *
      * @param transientProperty the transient property whose value will be set; required
      * @param propertyValue the property value to set; required
      * @param <T> the type of value to set
      * @throws NullPointerException if either of the params is {@code null}
      */
     <T> void set(TransientProperty<T> transientProperty, T propertyValue);

     void set(Collection<EntityField<E, ?>> fields, MultiFieldValueSupplier<E> valueSupplier);

     <CHILD extends EntityType<CHILD>> void addChild(ChangeEntityCommand<CHILD> childCmd);

     void add(MissingChildrenSupplier<? extends EntityType> missingChildrenSupplier);
}
