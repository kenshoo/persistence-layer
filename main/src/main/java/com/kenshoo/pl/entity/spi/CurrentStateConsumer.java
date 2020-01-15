package com.kenshoo.pl.entity.spi;

import com.kenshoo.pl.entity.*;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * This interface is implemented by pluggable components that require the current state of entities. For instance, a validator
 * that needs a profile ID of an entity, would indirectly implement this interface.
 * <b>Client classes should not implement this interface directly, it is used internally by the framework.</b>
 */
public interface CurrentStateConsumer<E extends EntityType<E>> {

    default SupportedChangeOperation getSupportedChangeOperation() {
        return SupportedChangeOperation.CREATE_UPDATE_AND_DELETE;
    }

    default Stream<? extends EntityField<?, ?>> getRequiredFields(Collection<? extends ChangeEntityCommand<E>> commands, ChangeOperation changeOperation) {
        return Stream.empty();
    }

    default Stream<? extends EntityField<?, ?>> requiredFields(Collection<? extends EntityField<E, ?>> fieldsToUpdate, ChangeOperation changeOperation) {
        return Stream.empty();
    }


    static <E extends EntityType<E>> Predicate<CurrentStateConsumer<E>> supporting(ChangeOperation op) {
        return consumer -> consumer.getSupportedChangeOperation().supports(op);
    }

    static <E extends EntityType<E>> Predicate<ChangeOperation> supporting(CurrentStateConsumer<E> consumer) {
        return op -> consumer.getSupportedChangeOperation().supports(op);
    }
}
