package com.kenshoo.pl.entity;

/**
 * The interface of any field -&gt; value collection.
 */
public interface FieldsValueMap<E extends EntityType<E>> {

    /**
     * Returns <code>true</code> is the map has a value for the given field
     */
    <T> boolean containsField(EntityField<E, T> field);

    /**
     * Returns the value of the specified field.
     *
     * @param field field to query
     * @param <T>   type of the field
     * @return the value of the field. Can be <code>null</code>.
     * @throws RuntimeException if the field is not present in the map
     */
    <T> T get(EntityField<E, T> field);

    /**
     * @param field field whose value should be fetched
     * @param <T> the type of the value in the field
     * @return the field value if not-<code>null</code>; or <code>Triptional.nullInstance()</code> if <code>null</code>; or <code>Triptional.absent()</code> if the field doesn't exist
     */
    default <T> Triptional<T> safeGet(final EntityField<E, T> field) {
        if (containsField(field)) {
            return Triptional.of(get(field));
        }
        return Triptional.absent();
    }
}
