package com.kenshoo.pl.entity;

import com.kenshoo.jooq.DataTable;
import org.jooq.Identity;
import org.jooq.Record;
import org.jooq.TableField;

import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Handles the transformation of a value for the entity field this adapter is attached to,
 * to the database and vice versa.
 *
 * @param <T> type of the entity field
 */
public interface EntityFieldDbAdapter<T> {

    /**
     * @return the table this entity fields maps to
     */
    DataTable getTable();

    /**
     * @return the table fields this entity field maps to
     */
    Stream<TableField<Record, ?>> getTableFields();

    /**
     * @return the first table field that this entity field maps to
     * @throws IllegalStateException if there are no fields
     */
    default TableField<Record, ?> getFirstTableField() {
        return getTableFields().findFirst()
                               .orElseThrow(() -> new IllegalStateException("There must be at least one field but none found"));
    }

    /**
     * @param value value of entity field to translate
     * @return the values for the fields returned by {@link #getTableFields()}, in the same order
     */
    Stream<Object> getDbValues(T value);

    /**
     * @param value value of entity field to translate
     * @return the first value for the fields returned by {@link #getTableFields()}, in field order
     * @throws IllegalStateException if there are no fields
     */
    default Object getFirstDbValue(T value) {
        return getDbValues(value).findFirst()
                                 .orElseThrow(() -> new IllegalStateException("There must be at least DB value but none found"));
    }

    /**
     * Composes the value of the entity field out of values of individual table fields. The iterator passed to
     * this method is positioned at the value of the first field returned by {@link #getTableFields()} and the
     * following values correspond to the rest of the fields in the same order.
     *
     * @param valuesIterator iterator positioned at the start of values for {@link #getTableFields()}
     * @return the value of entity field composed out of DB values taken from the supplied iterator
     */
    T getFromRecord(Iterator<Object> valuesIterator);

    default boolean isIdentityField() {
        return Optional.ofNullable(getTable().getIdentity())
                       .map(Identity::getField)
                       .map(identityField -> getTableFields().anyMatch(field -> field == identityField))
                       .orElse(false);
    }
}
