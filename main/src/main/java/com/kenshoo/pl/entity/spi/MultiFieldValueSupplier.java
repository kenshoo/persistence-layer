package com.kenshoo.pl.entity.spi;

import com.kenshoo.pl.entity.Entity;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.FieldsValueMap;

import java.util.Collection;

/**
 * A "delayed" supplied that specifies new values for a set of fields. A more complex version of {@link FieldValueSupplier}.
 *
 * @param <E> entity type
 * @see com.kenshoo.pl.entity.ChangeEntityCommand#set(Collection, MultiFieldValueSupplier)
 */
public interface MultiFieldValueSupplier<E extends EntityType<E>> extends  FetchEntityFields {

    /**
     * Returns a set of new values given an existing entity. In most of the cases should use {@link com.kenshoo.pl.entity.FieldsValueMapImpl}.
     *
     * @param entity entity before the change
     * @return new values
     */
    FieldsValueMap<E> supply(Entity entity) throws ValidationException;

}
