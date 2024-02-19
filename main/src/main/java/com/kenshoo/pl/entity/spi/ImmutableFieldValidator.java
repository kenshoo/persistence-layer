package com.kenshoo.pl.entity.spi;

import com.kenshoo.pl.entity.CurrentEntityState;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;

import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A validator that prevents a field from being modified (can only be set by a CREATE command).
 *
 * @param <E> entity type
 * @param <T> immutable field type
 */
public interface ImmutableFieldValidator<E extends EntityType<E>, T> extends ChangeValidator {

    /**
     * @return the field that should not be modified
     */
    EntityField<E, T> immutableField();

    /**
     * @return the error code to return if a modification of the field is attempted
     */
    String getErrorCode();

    /**
     * @return a list of fields to fetch.
     */
    default Stream<EntityField<?, ?>> fetchFields() {
        return Stream.of();
    }

    /**
     * @return Predicate when should validate field. It is used together with fetchFields().
     * The predicate is evaluated on the current state of the entity See {@link CurrentEntityState}.
     */
    default Predicate<CurrentEntityState> immutableWhen() {
        return e -> true;
    }


}
