package com.kenshoo.pl.entity;

import java.util.List;

import static java.util.Collections.emptyList;

public interface Entity {

    boolean containsField(EntityField<?, ?> field);

    <T> T get(EntityField<?, T> field);

    /**
     * @param field the field whose value should be fetched
     * @param <T> the type of value in the field
     * @return the field value if not <code>null</code>; or <code>Triptional.nullInstance()</code> if <code>null</code>; or <code>Triptional.absent()</code> if the field doesn't exist
     */
    default <T> Triptional<T> safeGet(final EntityField<?, T> field) {
        if (containsField(field)) {
            return Triptional.of(get(field));
        }
        return Triptional.absent();
    }

    default <E extends EntityType<E>> List<FieldsValueMap<E>> getMany(E type) {
        return emptyList();
    }

}
