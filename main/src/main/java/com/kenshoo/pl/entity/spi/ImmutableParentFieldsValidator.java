package com.kenshoo.pl.entity.spi;

import com.kenshoo.pl.entity.*;

import java.util.function.Predicate;
import java.util.stream.Stream;

public interface ImmutableParentFieldsValidator<E extends EntityType<E>> extends ChangeValidator {

    /**
     * @return the fields of the parent entities whose value is required for the validation
     */
    Stream<EntityField<?, ?>> parentsFields();

    /**
     * Called by the framework to validate the fields in entity change. The implementation can query the <code>entity</code> only for
     * the fields it has declared in {@link #parentsFields()}.
     * @return Predicate when parent immutable.
     */
    default Predicate<Entity> immutableWhen() {
        return e -> true;
    }

    /**
     * The implementation can query the <code>entity</code> only for
     * the fields it has declared in {@link #parentsFields()}.
     *
     * @param entityChange entity change
     * @param currentState existing entity
     *
     * @return generate error
     */
    ValidationError errorFor(EntityChange<E> entityChange, Entity currentState);
}
