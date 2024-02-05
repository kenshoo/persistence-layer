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

    protected <T> MutableEntityField<E, T> field(final TableField<Record, T> tableField) {
        return field(tableField, IdentityValueConverter.getInstance(tableField.getType()));
    }

    protected <T, DBT> MutableEntityField<E, T> field(final TableField<Record, DBT> tableField, final ValueConverter<T, DBT> valueConverter) {
        return field(tableField, valueConverter, Objects::equals);
    }

    protected <T, DBT> MutableEntityField<E, T> field(final TableField<Record, DBT> tableField,
                                                      final ValueConverter<T, DBT> valueConverter,
                                                      final ValueConverter<T, String> stringValueConverter) {
        return field(tableField, valueConverter, stringValueConverter, Objects::equals);
    }

    protected <T> MutableEntityField<E, T> field(final TableField<Record, T> tableField,
                                                 final EntityValueEqualityFunction<T> valueEqualityFunction) {
        return field(tableField,
                IdentityValueConverter.getInstance(tableField.getType()),
                createStringValueConverter(tableField.getType()),
                valueEqualityFunction);
    }

    protected <T, DBT> MutableEntityField<E, T> field(final TableField<Record, DBT> tableField,
                                                      final ValueConverter<T, DBT> valueConverter,
                                                      final EntityValueEqualityFunction<T> valueEqualityFunction) {
        return field(tableField, valueConverter, createStringValueConverter(valueConverter.getValueClass()), valueEqualityFunction);
    }

    protected <T, DBT> MutableEntityField<E, T> field(final TableField<Record, DBT> tableField,
                                                      final ValueConverter<T, DBT> valueConverter,
                                                      final ValueConverter<T, String> stringValueConverter,
                                                      final EntityValueEqualityFunction<T> valueEqualityFunction) {
        return addField(
                EntityFieldImpl.<E, T>builder(this)
                        .withDbAdapter(new SimpleEntityFieldDbAdapter<>(tableField, valueConverter))
                        .withStringValueConverter(stringValueConverter)
                        .withValueEqualityFunction(valueEqualityFunction)
                        .build()
        );
    }

    protected <T> MutableEntityField<E, T> field(EntityFieldDbAdapter<T> dbAdapter, ValueConverter<T, String> stringValueConverter) {
        return addField(
                EntityFieldImpl.<E, T>builder(this)
                        .withDbAdapter(dbAdapter)
                        .withStringValueConverter(stringValueConverter)
                        .withValueEqualityFunction(Objects::equals)
                        .build()
        );
    }

    protected <T, T1> MutableEntityField<E, T> virtualField(final EntityField<E, T1> field1,
                                                            final Function<T1, T> translator,
                                                            final ValueConverter<T, String> stringValueConverter,
                                                            final EntityValueEqualityFunction<T> valueEqualityFunction) {
        return virtualField(new VirtualEntityFieldDbAdapter<>(field1.getDbAdapter(), translator), stringValueConverter, valueEqualityFunction);
    }

    protected <T> MutableEntityField<E, T> virtualField(final DataTable table,
                                                        final ValueConverter<T, String> stringValueConverter,
                                                        final EntityValueEqualityFunction<T> valueEqualityFunction) {
        return virtualField(new EmptyVirtualEntityFieldDbAdapter<>(table), stringValueConverter, valueEqualityFunction);
    }

    protected <T, T1, T2> MutableEntityField<E, T> virtualField(final EntityField<E, T1> field1,
                                                                final EntityField<E, T2> field2,
                                                                final BiFunction<T1, T2, T> combiner,
                                                                final ValueConverter<T, String> stringValueConverter,
                                                                final EntityValueEqualityFunction<T> valueEqualityFunction) {
        return virtualField(new VirtualEntityFieldDbAdapter2<>(field1.getDbAdapter(), field2.getDbAdapter(), combiner), stringValueConverter, valueEqualityFunction);
    }

    private <T> MutableEntityField<E, T> virtualField(final EntityFieldDbAdapter<T> entityFieldDbAdapter,
                                                      final ValueConverter<T, String> stringValueConverter,
                                                      final EntityValueEqualityFunction<T> valueEqualityFunction) {
        return addField(
                VirtualEntityFieldImpl.<E, T>builder(this)
                        .withDbAdapter(entityFieldDbAdapter)
                        .withStringValueConverter(stringValueConverter)
                        .withValueEqualityFunction(valueEqualityFunction)
                        .build());
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
        final var field = PrototypedEntityFieldImpl.<E, T>builder(this)
                .withDbAdapter(new SimpleEntityFieldDbAdapter<>(tableField, valueConverter))
                .withStringValueConverter(createStringValueConverter(valueConverter.getValueClass()))
                .withValueEqualityFunction(valueEqualityFunction)
                .withEntityFieldPrototype(entityFieldPrototype)
                .build();
        prototypedFields.add(field);
        return addField(field);
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
