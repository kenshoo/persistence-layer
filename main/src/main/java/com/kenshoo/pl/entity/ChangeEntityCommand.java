package com.kenshoo.pl.entity;

import com.kenshoo.pl.entity.internal.EntityFieldImpl;
import com.kenshoo.pl.entity.internal.EntityTypeReflectionUtil;
import com.kenshoo.pl.entity.internal.LazyDelegatingMultiSupplier;
import com.kenshoo.pl.entity.internal.MissingChildrenSupplier;
import com.kenshoo.pl.entity.spi.*;

import java.util.*;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static java.util.Objects.requireNonNull;
import static org.jooq.lambda.Seq.seq;

abstract public class ChangeEntityCommand<E extends EntityType<E>> implements MutableCommand<E> {

    private final E entityType;
    private final Map<EntityField<E, ?>, Object> values = new HashMap<>();
    private final Map<TransientProperty<?>, Object> transientProperties = new HashMap<>(0);
    private final Map<EntityField<E, ?>, FieldValueSupplierDelegate<E,?>> suppliers = new HashMap<>();
    private final List<ChangeEntityCommand<? extends EntityType>> children = newArrayListWithCapacity(1);
    private final List<MissingChildrenSupplier<? extends EntityType>> missingChildrenSuppliers = newArrayListWithCapacity(1);

    private ChangeEntityCommand parent;
    private Identifier<E> keysToParent;

    public ChangeEntityCommand(E entityType) {
        this.entityType = entityType;
    }

    public E getEntityType() {
        return entityType;
    }

    @Override
    public <T> void set(EntityField<E, T> field, T newValue) {
        values.put(field, newValue);
    }

    @Override
    public <T> void set(EntityFieldPrototype<T> fieldPrototype, T newValue) {
        EntityField<E, T> entityField = findFieldByPrototype(fieldPrototype);
        set(entityField, newValue);
    }

    @Override
    public <T> void set(EntityField<E, T> field, FieldValueSupplier<T> valueSupplier) {
        suppliers.put(field, new SingleFieldValueSupplierDelegate<>(valueSupplier));
    }

    @Override
    public <T> void set(EntityFieldPrototype<T> fieldPrototype, FieldValueSupplier<T> valueSupplier) {
        EntityField<E, T> entityField = findFieldByPrototype(fieldPrototype);
        set(entityField, valueSupplier);
    }

    @Override
    public void set(Collection<EntityField<E, ?>> fields, MultiFieldValueSupplier<E> valueSupplier) {
        LazyDelegatingMultiSupplier<E> delegatingMultiSupplier = new LazyDelegatingMultiSupplier<>(valueSupplier);

        for (EntityField<E, ?> field : fields) {
            addAsSingleValueSupplier(field, delegatingMultiSupplier);
        }
    }

    @Override
    public <T> void set(final TransientProperty<T> transientProperty, final T propertyValue) {
        requireNonNull(transientProperty, "A transient property must be provided");
        requireNonNull(propertyValue, "A property value must be provided");

        transientProperties.put(transientProperty, propertyValue);
    }

    private <T> void addAsSingleValueSupplier(final EntityField<E, T> entityField, final MultiFieldValueSupplier<E> delegatingSupplier) {
        suppliers.put(entityField, new MultiFieldValueSupplierDelegate<>(delegatingSupplier, entityField));
    }

    @Override
    public Stream<EntityField<E, ?>> getChangedFields() {
        // HashMap creates keySet/entrySet on demand so if the map is empty, calling these method implicitly increases its
        // memory consumption. Since in many cases the suppliers HashMap is empty, we can short-circuit this.
        if (suppliers.isEmpty()) {
            return values.keySet().stream();
        } else {
            return Stream.concat(this.values.keySet().stream(), suppliers.keySet().stream()).distinct();
        }
    }

    @Override
    public Stream<FieldChange<E, ?>> getChanges() {
        //noinspection unchecked
        return values.entrySet().stream().map(entry -> new FieldChange(entry.getKey(), entry.getValue()));
    }

    @Override
    public <T> boolean containsField(EntityField<E, T> field) {
        return isFieldChanged(field);
    }

    @Override
    public boolean isFieldChanged(EntityField<E, ?> field) {
        return values.containsKey(field);
    }

    @Override
    public <T> T get(EntityField<E, T> field) {
        //noinspection unchecked,SuspiciousMethodCalls
        T value = (T) values.get(field);
        if (value == null && !values.containsKey(field)) {
            throw new IllegalArgumentException("No value supplied for field " + field);
        }
        return value;
    }

    @Override
    public <T> Optional<T> get(final TransientProperty<T> transientProperty) {
        requireNonNull(transientProperty, "A transient property must be specified");
        //noinspection unchecked
        return Optional.ofNullable(transientProperties.get(transientProperty))
                .map(transientVal -> (T) transientVal);
    }

    public <CHILD extends EntityType<CHILD>> void addChild(ChangeEntityCommand<CHILD> childCmd) {
        children.add(childCmd);
        childCmd.parent = this;
    }

    @Override
    public <CHILD extends EntityType<CHILD>> Stream<ChangeEntityCommand<CHILD>> getChildren(CHILD type) {
        //noinspection unchecked
        return children.stream().filter(cmd -> cmd.getEntityType() == type).map(cmd -> (ChangeEntityCommand<CHILD>) cmd);
    }

    @Override
    public Stream<ChangeEntityCommand<? extends EntityType>> getChildren() {
        return children.stream();
    }

    void unset(EntityField<E, ?> field) {
        values.remove(field);
    }

    void resolveSuppliers(CurrentEntityState currentState) throws ValidationException {
        // HashMap creates keySet/entrySet on demand so if the map is empty, calling these method implicitly increases its
        // memory consumption. Since in many cases the suppliers HashMap is empty, we can short-circuit this.
        if (suppliers.isEmpty()) {
            return;
        }
        for (Map.Entry<EntityField<E, ?>, FieldValueSupplierDelegate<E, ?>> entry : suppliers.entrySet()) {
            try {
                values.put(entry.getKey(), entry.getValue().supply(currentState));
            } catch (NotSuppliedException ignore) {
            }
        }
    }

    Stream<? extends CurrentStateConsumer<E>> getCurrentStateConsumers() {
        if(suppliers.isEmpty()) {
            return Stream.empty();
        }
        return suppliers.values().stream().distinct();
    }

    private <T> EntityField<E, T> findFieldByPrototype(EntityFieldPrototype<T> fieldPrototype) {
        Set<EntityField<E, T>> entityFields = EntityTypeReflectionUtil.getFieldsByPrototype(entityType, fieldPrototype);
        if (entityFields.isEmpty()) {
            throw new IllegalArgumentException("Entity " + entityType + " doesn't have a field with prototype " + fieldPrototype);
        }
        if (entityFields.size() > 1) {
            // We should fix custom params and declare that only one field can have a given prototype
            throw new IllegalStateException("Entity " + entityType + " has more than one field with prototype " + fieldPrototype);
        }
        return entityFields.iterator().next();
    }

    static <E extends EntityType<E>> void copy(ChangeEntityCommand<E> toCommand, Identifier<E> identifier) {
        copyFields(toCommand, identifier.getUniqueKey().getFields(), identifier);
    }

    static private <E extends EntityType<E>> void copyFields(ChangeEntityCommand<E> toCommand, EntityField<E, ?>[] fields, FieldsValueMap<E> fieldsValueMap) {
        //noinspection unchecked
        Stream.of(fields).map(field -> (EntityFieldImpl<E, ?>) field).forEach(field -> toCommand.set(field, fieldsValueMap.get(field)));
    }


    public Identifier<E> getKeysToParent() {
        return keysToParent;
    }

    void setKeysToParent(Identifier<E> keysToParent) {
        this.keysToParent = keysToParent;
    }

    public void updateOperator(ChangeOperation changeOperation) {
    }

    public ChangeEntityCommand<?> getParent() {
        return parent;
    }

    public void add(MissingChildrenSupplier<? extends EntityType> missingChildrenSupplier) {
        missingChildrenSuppliers.add(missingChildrenSupplier);
    }

    List<MissingChildrenSupplier<? extends EntityType>> getMissingChildrenSuppliers() {
        return missingChildrenSuppliers;
    }

    <CHILD extends EntityType<CHILD>> Optional<MissingChildrenSupplier<CHILD>> getMissingChildrenSupplier(CHILD entityType) {
        final Optional<MissingChildrenSupplier<? extends EntityType>> missingChildrenSupplier = seq(this.missingChildrenSuppliers).findFirst(supplier -> entityType.equals(supplier.getChildType()));
        return missingChildrenSupplier.map(supplier -> (MissingChildrenSupplier<CHILD>) supplier);
    }


    interface FieldValueSupplierDelegate<E extends EntityType<E>, T> extends FieldValueSupplier<T>, CurrentStateConsumer<E> {

    }

    static private class SingleFieldValueSupplierDelegate<E extends EntityType<E>, T> implements FieldValueSupplierDelegate<E, T> {

        private final FieldValueSupplier<T> valueSupplier;

        private SingleFieldValueSupplierDelegate(FieldValueSupplier<T> supplier) {
            this.valueSupplier = supplier;
        }

        @Override
        public T supply(CurrentEntityState currentState) throws ValidationException, NotSuppliedException {
            return valueSupplier.supply(currentState);
        }

        @Override
        public Stream<EntityField<?, ?>> fetchFields(ChangeOperation changeOperation) {
            return valueSupplier.fetchFields(changeOperation);
        }

        @Override
        public Stream<? extends EntityField<?, ?>> requiredFields(Collection<? extends EntityField<E, ?>> fieldsToUpdate, ChangeOperation changeOperation) {
            return fetchFields(changeOperation);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SingleFieldValueSupplierDelegate<?, ?> that = (SingleFieldValueSupplierDelegate<?, ?>) o;
            return Objects.equals(valueSupplier, that.valueSupplier);
        }

        @Override
        public int hashCode() {
            return Objects.hash(valueSupplier);
        }
    }

    static private class MultiFieldValueSupplierDelegate<E extends EntityType<E>, T> implements FieldValueSupplierDelegate<E, T> {

        private final MultiFieldValueSupplier<E> multiValueSupplier;
        private final EntityField<E, T> entityField;

        private MultiFieldValueSupplierDelegate(MultiFieldValueSupplier<E> supplier, EntityField<E, T> entityField) {
            this.multiValueSupplier = supplier;
            this.entityField = entityField;
        }

        @Override
        public T supply(CurrentEntityState currentState) throws ValidationException {
            FieldsValueMap<E> result = multiValueSupplier.supply(currentState);
            if (result.containsField(entityField)) {
                return result.get(entityField);
            } else {
                throw new NotSuppliedException();
            }
        }

        @Override
        public Stream<EntityField<?, ?>> fetchFields(ChangeOperation changeOperation) {
            return multiValueSupplier.fetchFields(changeOperation);
        }

        @Override
        public Stream<? extends EntityField<?, ?>> requiredFields(Collection<? extends EntityField<E, ?>> fieldsToUpdate, ChangeOperation changeOperation) {
            return fetchFields(changeOperation);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MultiFieldValueSupplierDelegate<?, ?> that = (MultiFieldValueSupplierDelegate<?, ?>) o;
            return Objects.equals(multiValueSupplier, that.multiValueSupplier);
        }

        @Override
        public int hashCode() {
            return Objects.hash(multiValueSupplier);
        }
    }
}
