package com.kenshoo.pl.entity;

import java.util.Optional;
import java.util.stream.Stream;

public interface EntityChange<E extends EntityType<E>> extends FieldsValueMap<E> {

    E getEntityType();

    /**
     * @return a stream of fields modified by this change, each field being included at most once
     */
    Stream<EntityField<E, ?>> getChangedFields();

    /**
     * Returns the value of the given transient property.
     *
     * @param transientProperty transient property to query; required
     * @param <T> type of the value
     * @return the value of the property, or {@code Optional.empty()} if {@code null} or absent.
     */
    <T> Optional<T> get(final TransientProperty<T> transientProperty);

    /**
     * @return a stream of the individual field changes modified by this change. The stream does not include
     * suppliers, only fixed values
     */
    Stream<FieldChange<E, ?>> getChanges();

    /**
     * @return <code>true</code> if the field is affected by this change, without checking suppliers
     * @deprecated use {@link #isFieldChangedIncludingSuppliers(EntityField)}
     */
    boolean isFieldChanged(EntityField<E, ?> field);

    /**
     * Same as {@link #isFieldChanged}, but also checks whether there is a supplier for the field if no value provided.
     * @return <code>true</code> if the field is affected by this change, either directly or through a supplier. A shorthand for <code>getChangedFields().contains(field)</code>
     */
    boolean isFieldChangedIncludingSuppliers(EntityField<E, ?> field);

    /**
     * @return the identifier of the entity being changed. Works only for update or delete changes, not for create
     */
    Identifier<E> getIdentifier();

    /**
     * @return of stream of sub changes of the entity
     */
    Stream<? extends EntityChange<? extends EntityType>> getChildren();
    /**
     * @return of stream of sub changes of the entity by child type
     */
    <CHILD extends EntityType<CHILD>> Stream<? extends EntityChange<CHILD>> getChildren(CHILD type);
    /**
     * @return the parent identifier of the entity
     */
    Identifier<E> getKeysToParent();
    /**
     * @return return change operation
     */
    ChangeOperation getChangeOperation();
    /**
     * @return return if entity must be found for entity change
     */
    default boolean allowMissingEntity() {
        return false;
    }


}
