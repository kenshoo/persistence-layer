package com.kenshoo.pl.entity.spi;

import com.kenshoo.pl.entity.*;

import java.util.function.Predicate;
import java.util.stream.Stream;

public interface AncestorsValidator<E extends EntityType<E>> extends ChangeValidator {

    /**
     * @return the fields of the parent entities whose value is required for the validation
     */
    Stream<EntityField<?, ?>> ancestorsFields();

    /**
     * Called by the framework to check if the change is restricted by ancestors. The implementation can query the <code>entity</code> only for
     * the fields it has declared in {@link #ancestorsFields()}.
     * @return Predicate when parent immutable.
     */
    default Predicate<Entity> ancestorsRestriction() {
        return e -> true;
    }

    /**
     * The implementation can query the <code>entity</code> only for
     * the fields it has declared in {@link #ancestorsFields()}.
     *
     * @param entityChange entity change
     * @param currentState existing entity
     *
     * @return generate error
     */
    ValidationError errorFor(EntityChange<E> entityChange, Entity currentState);
}
