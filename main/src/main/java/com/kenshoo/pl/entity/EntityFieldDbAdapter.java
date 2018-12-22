package com.kenshoo.pl.entity;

import com.kenshoo.pl.jooq.DataTable;
import org.jooq.Record;
import org.jooq.TableField;

import java.util.Iterator;
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
     * @return the list of the table fields this entity field maps to
     */
    Stream<TableField<Record, ?>> getTableFields();

    /**
     * @param value value of entity field to translate
     * @return the list of values for the fields returned by {@link #getTableFields()}, in the same order
     */
    Stream<Object> getDbValues(T value);

    /**
     * Composes the value of the entity field out of values of individual table fields. The iterator passed to
     * this method is positioned at the value of the first field returned by {@link #getTableFields()} and the
     * following values correspond to the rest of the fields in the same order.
     *
     * @param valuesIterator iterator positioned at the start of values for {@link #getTableFields()}
     * @return the value of entity field composed out of DB values taken from the supplied iterator
     */
    T getFromRecord(Iterator<Object> valuesIterator);
}
