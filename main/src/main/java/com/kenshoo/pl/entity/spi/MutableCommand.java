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
      * Add or replace the value of a given transient field in this command.<br>
      *
      * NOTE that unlike regular entity fields, there is no distinction between a {@code null} value and an absent value, therefore:
      * <ul>
      * <li>Adding a {@code null} value to a transient field that is not yet in the command, will have no effect.
      * <li>Replacing a transient field value with {@code null} will effectively remove the field from the command.
      * </ul>
      *
      * @param transientField the transient field whose value will be set; required
      * @param newValue the new value to set
      * @param <T> the type of value to set
      * @throws NullPointerException if {@code transientField} is {@code null}
      */
     <T> void set(TransientEntityField<E, T> transientField, T newValue);

     void set(Collection<EntityField<E, ?>> fields, MultiFieldValueSupplier<E> valueSupplier);

     <CHILD extends EntityType<CHILD>> void addChild(ChangeEntityCommand<CHILD> childCmd);

     void add(MissingChildrenSupplier<? extends EntityType> missingChildrenSupplier);
}
