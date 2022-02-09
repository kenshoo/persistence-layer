package com.kenshoo.pl.entity;

import com.google.common.base.Supplier;
import com.google.common.collect.BiMap;
import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.annotation.IdGeneration;
import com.kenshoo.pl.entity.converters.EnumAsStringValueConverter;
import com.kenshoo.pl.entity.converters.IdentityValueConverter;
import com.kenshoo.pl.entity.equalityfunctions.EntityValueEqualityFunction;
import com.kenshoo.pl.entity.internal.*;
import org.jooq.Record;
import org.jooq.TableField;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.base.Suppliers.memoize;
import static com.kenshoo.pl.entity.internal.EntityTypeReflectionUtil.getFieldAnnotation;

public abstract class AbstractEntityType<E extends EntityType<E>> implements EntityType<E> {

    private final Supplier<Optional<IdField<E>>> idField = memoize(this::scanForIdField);
    private EntityField<E, Object> primaryIdentityField;

    private final String name;
    private Collection<EntityField<E, ?>> fields = new ArrayList<>();
    private Collection<PrototypedEntityField<E, ?>> prototypedFields = new ArrayList<>();

    private final Supplier<BiMap<String, EntityField<E, ?>>> fieldNameMappingSupplier = memoize(() -> EntityTypeReflectionUtil.getFieldToNameBiMap(AbstractEntityType.this));

    protected AbstractEntityType(String name) {
        this.name = name;
    }

    protected <T> EntityField<E, T> field(TableField<Record, T> tableField) {
        return field(tableField, IdentityValueConverter.getInstance(tableField.getType()));
    }

    protected <T, DBT> EntityField<E, T> field(TableField<Record, DBT> tableField, ValueConverter<T, DBT> valueConverter) {
        return field(tableField, valueConverter, Objects::equals);
    }

    protected <T, DBT> EntityField<E, T> field(TableField<Record, DBT> tableField, ValueConverter<T, DBT> valueConverter, ValueConverter<T, String> stringValueConverter) {
        return field(tableField, valueConverter, stringValueConverter, Objects::equals);
    }

    protected <T> EntityField<E, T> field(TableField<Record, T> tableField, EntityValueEqualityFunction<T> valueEqualityFunction) {
        return field(tableField, IdentityValueConverter.getInstance(tableField.getType()), createStringValueConverter(tableField.getType()), valueEqualityFunction);
    }

    protected <T, DBT> EntityField<E, T> field(TableField<Record, DBT> tableField, ValueConverter<T, DBT> valueConverter, EntityValueEqualityFunction<T> valueEqualityFunction) {
        return field(tableField, valueConverter, createStringValueConverter(valueConverter.getValueClass()), valueEqualityFunction);
    }

    protected <T, DBT> EntityField<E, T> field(TableField<Record, DBT> tableField, ValueConverter<T, DBT> valueConverter,
                                               ValueConverter<T, String> stringValueConverter, EntityValueEqualityFunction<T> valueEqualityFunction) {
        return addField(new EntityFieldImpl<>(this, new SimpleEntityFieldDbAdapter<>(tableField, valueConverter), stringValueConverter, valueEqualityFunction));
    }

    protected <T> EntityField<E, T> field(EntityFieldDbAdapter<T> dbAdapter, ValueConverter<T, String> stringValueConverter) {
        return addField(new EntityFieldImpl<>(this, dbAdapter, stringValueConverter, Objects::equals));
    }

    protected <T, T1> EntityField<E, T> virtualField(EntityField<E, T1> field1, Function<T1, T> translator,
                                                     ValueConverter<T, String> stringValueConverter, EntityValueEqualityFunction<T> valueEqualityFunction) {
        return virtualField(new VirtualEntityFieldDbAdapter<>(field1.getDbAdapter(), translator), stringValueConverter, valueEqualityFunction);
    }

    protected <T> EntityField<E, T> virtualField(DataTable table,
                                                 ValueConverter<T, String> stringValueConverter,
                                                 EntityValueEqualityFunction<T> valueEqualityFunction) {
        return virtualField(new EmptyVirtualEntityFieldDbAdapter<>(table), stringValueConverter, valueEqualityFunction);
    }

    protected <T, T1, T2> EntityField<E, T> virtualField(EntityField<E, T1> field1, EntityField<E, T2> field2, BiFunction<T1, T2, T> combiner,
                                                         ValueConverter<T, String> stringValueConverter, EntityValueEqualityFunction<T> valueEqualityFunction) {
        return virtualField(new VirtualEntityFieldDbAdapter2<>(field1.getDbAdapter(), field2.getDbAdapter(), combiner), stringValueConverter, valueEqualityFunction);
    }

    private <T> EntityField<E, T> virtualField(EntityFieldDbAdapter<T> entityFieldDbAdapter,
                                               ValueConverter<T, String> stringValueConverter,
                                               EntityValueEqualityFunction<T> valueEqualityFunction) {
        return addField(new VirtualEntityFieldImpl<>(this, entityFieldDbAdapter, stringValueConverter, valueEqualityFunction));
    }

    private static <T> ValueConverter<T, String> createStringValueConverter(Class<T> valueClass) {
        if (Enum.class.isAssignableFrom(valueClass)) {
            //noinspection unchecked
            return (ValueConverter<T, String>) EnumAsStringValueConverter.create((Class<? extends Enum>) valueClass);
        }
        if (!CommonTypesStringConverter.isSupported(valueClass)) {
            throw new IllegalArgumentException("Class " + valueClass + " is not supported out of the box, please specify a converter to/from String");
        }
        return new CommonTypesStringConverter<>(valueClass);
    }

    protected <T> PrototypedEntityField<E, T> prototypedField(EntityFieldPrototype<T> entityFieldPrototype, TableField<Record, T> tableField) {
        return prototypedField(entityFieldPrototype, tableField, IdentityValueConverter.getInstance(tableField.getType()));
    }

    protected <T> PrototypedEntityField<E, T> prototypedField(EntityFieldPrototype<T> entityFieldPrototype, TableField<Record, T> tableField, EntityValueEqualityFunction<T> valueEqualityFunction) {
        return prototypedField(entityFieldPrototype, tableField, IdentityValueConverter.getInstance(tableField.getType()), valueEqualityFunction);
    }

    protected <T, DBT> PrototypedEntityField<E, T> prototypedField(EntityFieldPrototype<T> entityFieldPrototype, TableField<Record, DBT> tableField, ValueConverter<T, DBT> valueConverter) {
        return prototypedField(entityFieldPrototype, tableField, valueConverter, Objects::equals);
    }

    protected <T, DBT> PrototypedEntityField<E, T> prototypedField(EntityFieldPrototype<T> entityFieldPrototype, TableField<Record, DBT> tableField, ValueConverter<T, DBT> valueConverter, EntityValueEqualityFunction<T> valueEqualityFunction) {
        PrototypedEntityFieldImpl<E, T> field = new PrototypedEntityFieldImpl<>(this, entityFieldPrototype, new SimpleEntityFieldDbAdapter<>(tableField, valueConverter),
                createStringValueConverter(valueConverter.getValueClass()), valueEqualityFunction);
        prototypedFields.add(field);
        return addField(field);
    }

    /**
     * Add a transient property to this entity type with the given name.<br>
     * This method should only be used for populating a constant member of the entity, for example:
     *
     * <pre>
     * public final MyEntity extends AbstractEntityType&lt;MyEntity&gt; {
     *     public static final TransientEntityProperty&lt;MyEntity, String&gt; MY_TRANSIENT = transientProperty("myTransient");
     * }
     * </pre>
     *
     * @param name the name; must not be blank
     * @param <T> the type of value that this object can hold
     * @return the new transient property
     * @throws IllegalArgumentException if the given name is blank or {@code null}
     */
    protected <T> TransientEntityProperty<E, T> transientProperty(final String name) {
        return new TransientEntityPropertyImpl<>(this, name);
    }

    // Casting here because the identity field can be of arbitrary type, and we must be able to mutate its value in a command
    @SuppressWarnings("unchecked")
    private <T, F extends EntityField<E, T>> F addField(F field) {
        fields.add(field);
        if (primaryIdentityField == null && isPrimaryIdentityField(field)) {
            this.primaryIdentityField = (EntityField<E, Object>)field;
        }
        return field;
    }

    private boolean isPrimaryIdentityField(final EntityField<E, ?> field) {
        final EntityFieldDbAdapter<?> dbAdapter = field.getDbAdapter();
        return dbAdapter.getTable() == getPrimaryTable() && dbAdapter.isIdentityField();
    }

    @Override
    public String getName() {
        return name;
    }

    private Optional<IdField<E>> scanForIdField() {
        Optional<IdField<E>> idField = getFields()
                .map(field -> Optional.ofNullable(getFieldAnnotation(this, field, Id.class)).map(a -> new IdField<>((EntityField<E, ? extends Number>)field, a.value())))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();

        if (idField.isPresent() && !Number.class.isAssignableFrom(idField.get().getField().getValueClass())) {
            throw new RuntimeException("Field marked with " + Id.class.getName() + " should be either Integer or Long " +
                    ", field " + this.toFieldName(idField.get().getField()) + " is of type " + idField.get().getField().getStringValueConverter().getValueClass().getName());
        }
        return idField;
    }

    @Override
    public Optional<EntityField<E, ? extends Number>> getIdField() {
        return idField.get().map(IdField::getField);
    }

    @Override
    public Optional<EntityField<E, Object>> getPrimaryIdentityField() {
        return Optional.ofNullable(primaryIdentityField);
    }

    @Override
    public Optional<IdGeneration> getIdGeneration() {
        return idField.get().map(IdField::getIdGeneration);
    }

    @Override
    public Stream<EntityField<E, ?>> getFields() {
        return fields.stream();
    }

    @Override
    public Stream<PrototypedEntityField<E, ?>> getPrototypedFields() {
        return prototypedFields.stream();
    }

    @Override
    public EntityField<E, ?> getFieldByName(String name) {
        BiMap<String, EntityField<E, ?>> fieldToNameBiMap = fieldNameMappingSupplier.get();

        if (fieldToNameBiMap.containsKey(name)) {
            return fieldToNameBiMap.get(name);
        } else {
            throw new IllegalArgumentException("Requested EntityField does not exist: '" + name + "'.");
        }
    }

    @Override
    public String toFieldName(EntityField<E, ?> field) {
        BiMap<String, EntityField<E, ?>> fieldToNameBiMap = fieldNameMappingSupplier.get();
        return fieldToNameBiMap.inverse().get(field);
    }
}
