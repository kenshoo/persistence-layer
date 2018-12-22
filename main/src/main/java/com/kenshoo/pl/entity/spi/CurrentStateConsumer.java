package com.kenshoo.pl.entity.spi;

import com.kenshoo.pl.entity.ChangeEntityCommand;
import com.kenshoo.pl.entity.ChangeOperation;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * This interface is implemented by pluggable components that require the current state of entities. For instance, a validator
 * that needs a profile ID of an entity, would indirectly implement this interface.
 * <b>Client classes should not implement this interface directly, it is used internally by the framework.</b>
 */
public interface CurrentStateConsumer<E extends EntityType<E>> {
    Stream<? extends EntityField<?, ?>> getRequiredFields(Collection<? extends ChangeEntityCommand<E>> commands, ChangeOperation changeOperation);
}
