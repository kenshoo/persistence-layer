package com.kenshoo.pl.data;

import com.google.common.base.*;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.kenshoo.jooq.DataTable;
import com.kenshoo.jooq.FieldAndValue;
import org.apache.commons.lang3.ArrayUtils;
import org.jooq.*;
import org.jooq.impl.TableImpl;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Objects;

import static org.jooq.impl.Internal.createForeignKey;
import static org.jooq.impl.Internal.createUniqueKey;

public class ImpersonatorTable extends TableImpl<Record> implements DataTable {

    public static final String TEMP_TABLE_PREFIX = "tmp_";

    private final DataTable realTable;

    private final Supplier<UniqueKey<Record>> primaryKeySupplier = Suppliers.memoize(new PrimaryKeySupplier());
    private final Supplier<List<ForeignKey<Record, ?>>> foreignKeysSupplier = Suppliers.memoize(new ForeignKeysSupplier());

    public ImpersonatorTable(DataTable realTable) {
        super(TEMP_TABLE_PREFIX + realTable.getName());
        this.realTable = realTable;
    }

    public <T> TableField<Record, T> createField(Field<T> realTableField) {
        return createField(realTableField.getName(), realTableField.getDataType());
    }

    @Override
    public Collection<FieldAndValue<?>> getVirtualPartition() {
        return Collections2.transform(realTable.getVirtualPartition(), new Function<FieldAndValue<?>, FieldAndValue<?>>() {
            @Override
            public FieldAndValue<?> apply(FieldAndValue<?> input) {
                return transform(input);
            }
            private <T> FieldAndValue<T> transform(FieldAndValue<T> input) {
                return new FieldAndValue<>(getField(input.getField()), input.getValue());
            }
        });
    }

    @Override
    public UniqueKey<Record> getPrimaryKey() {
        return primaryKeySupplier.get();
    }

    @Override
    public List<ForeignKey<Record, ?>> getReferences() {
        return foreignKeysSupplier.get();
    }

    public <T> TableField<Record, T> getField(final TableField<Record, T> realTableField) {
        //noinspection unchecked
        return (TableField<Record, T>) Iterables.tryFind(Arrays.asList(fields()), new Predicate<Field<?>>() {
            @Override
            public boolean apply(Field<?> field) {
                return field.getName().equals(realTableField.getName());
            }
        }).orNull();
    }

    private class PrimaryKeySupplier implements Supplier<UniqueKey<Record>> {
        @Override
        public UniqueKey<Record> get() {
            return createPK();
        }
    }

    private class ForeignKeysSupplier implements Supplier<List<ForeignKey<Record, ?>>> {

        @Override
        public List<ForeignKey<Record, ?>> get() {
            return realTable.getReferences().stream()
                    .map(ImpersonatorTable.this::createFK)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toUnmodifiableList());
        }
    }

    private UniqueKey<Record> createPK() {
        UniqueKey<Record> primaryKey = realTable.getPrimaryKey();
        if (primaryKey == null) {
            return null;
        }
        TableField<Record, ?>[] transformedFields = transformFields(primaryKey.getFieldsArray());
        if (ArrayUtils.contains(transformedFields, null)) {
            // We don't have some of the fields of realTable PK
            return null;
        }
        //noinspection unchecked
        return createUniqueKey(ImpersonatorTable.this, transformedFields);
    }

    private ForeignKey<Record, ?> createFK(ForeignKey<Record, ?> foreignKey) {
        TableField<Record, ?>[] fields = transformFields(foreignKey.getFieldsArray());
        if (ArrayUtils.contains(fields, null)) {
            return null;
        }
        UniqueKey<Record> uniqueKey = cloneUniqueKey(foreignKey.getKey());
        return createForeignKey(uniqueKey, uniqueKey.getTable(), fields);
    }

    @SuppressWarnings("unchecked")
    private static UniqueKey<Record> cloneUniqueKey(UniqueKey<?> uniqueKey) {
        return createUniqueKey((Table<Record>) uniqueKey.getTable(), (TableField<Record, ?>[]) uniqueKey.getFieldsArray());
    }

    private TableField<Record, ?>[] transformFields(TableField<Record, ?>[] originalFields) {
        //noinspection unchecked
        return Arrays.stream(originalFields)
                .map(this::getField)
                .toArray(TableField[]::new);
    }
}
