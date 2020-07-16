package com.kenshoo.pl.entity.spi;

import com.kenshoo.pl.entity.*;

import java.util.function.Predicate;
import java.util.stream.Stream;

public interface AncestorsValidator extends ChangeValidator {

    /**
     * @return the fields of the ancestor entities whose value is required for the validation
     */
    Stream<EntityField<?, ?>> ancestorsFields();

    /**
     * Called by the framework to check if the change is restricted by ancestors. The implementation can query the <code>entity</code> only for
     * the fields it has declared in {@link #ancestorsFields()}.
     * @return Predicate when restricted by ancestors.
     */
    default Predicate<CurrentEntityState> ancestorsRestriction() {
        return e -> true;
    }

    /**
     * The implementation can query the <code>entity</code> only for
     * the fields it has declared in {@link #ancestorsFields()}.
     *
     * @param currentState existing entity state
     *
     * @return generate error
     */
    ValidationError errorFor(CurrentEntityState currentState);
}
