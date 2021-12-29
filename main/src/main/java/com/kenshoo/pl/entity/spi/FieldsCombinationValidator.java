package com.kenshoo.pl.entity.spi;

import com.kenshoo.pl.entity.CurrentEntityState;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.FieldsValueMap;
import com.kenshoo.pl.entity.ValidationError;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A validator that verifies that the resulting state of several fields is legal, no matter whether some of them
 * are already set or being modified by the command. For instance, if a validator validates that the value of field 'B'
 * is greater than the value of field 'A', it is going to be called when either 'B' or 'A' (or both) are being changed.
 */
public interface FieldsCombinationValidator<E extends EntityType<E>> extends ChangeValidator {

    /**
     * @return the list of fields whose combination is being validated; May contain fields of the current entity only.
     */
    Stream<EntityField<E, ?>> validatedFields();

    /**
     * Implements the validation and return an error if there is.
     *
     * @param fieldsValueMap the map containing the fields specified by {@link #validatedFields()}. If a field is
     *                       being modified by the command, its new value is going to be in the map, otherwise the
     *                       existing value would be passed.
     * @return validation error if there is or <code>null</code> if OK
     */
    ValidationError validate(FieldsValueMap<E> fieldsValueMap);

    /**
     * @return a list of fields to fetch. May contain fields of parent entities only
     */
    default Stream<EntityField<?, ?>> fetchFields() {
        return Stream.of();
    }

    /**
     * @return Predicate when should validate fields. It is used together with fetchFields(), so only parent fields can be referenced here.
     */
    default Predicate<CurrentEntityState> validateWhen() {
        return e -> true;
    }

    /**
     * @return a field's substitutions.
     */
    default Stream<Substitution<E, ?>> substitutions() {
        return Stream.of();
    }

    interface Substitution<E extends EntityType<E>, T> {

        /**
         * @return the field to substitute
         */
        EntityField<E, T> overrideField();

        /**
         * @return the condition when field should be substituted
         */
        Predicate<T> overrideWhen();
        /**
         * @return the field substitution function
         */
        Function<CurrentEntityState, T> overrideHow();
    }
}
