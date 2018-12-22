package com.kenshoo.pl.data;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.kenshoo.pl.jooq.DataTable;
import com.kenshoo.pl.jooq.FieldAndValue;
import org.apache.commons.lang3.ArrayUtils;
import org.jooq.*;
import org.jooq.impl.AbstractKeys;
import org.jooq.impl.TableImpl;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ImpersonatorTable extends TableImpl<Record> implements DataTable {

    public static final String TEMP_TABLE_PREFIX = "tmp_";

    private final Keys keys = new Keys();
    private final DataTable realTable;

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
        return keys.createPK();
    }

    @Override
    public List<ForeignKey<Record, ?>> getReferences() {
        // Transform is going to return nulls for foreign keys composed of fields not present in impersonator table,
        // filtering them out
        return Lists.newArrayList(Collections2.filter(Lists.transform(realTable.getReferences(), new Function<ForeignKey<Record, ?>, ForeignKey<Record, ?>>() {
            @Override
            public ForeignKey<Record, ?> apply(ForeignKey<Record, ?> foreignKey) {
                return keys.createFK(foreignKey);
            }
        }), Predicates.notNull()));
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

    private class Keys extends AbstractKeys {
        public UniqueKey<Record> createPK() {
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

        public ForeignKey<Record, ?> createFK(ForeignKey<Record, ?> foreignKey) {
            TableField<Record, ?>[] fields = transformFields(foreignKey.getFieldsArray());
            if (ArrayUtils.contains(fields, null)) {
                return null;
            }
            //noinspection unchecked
            return createForeignKey(foreignKey.getKey(), foreignKey.getTable(), fields);
        }

        private TableField<Record, ?>[] transformFields(TableField<Record, ?>[] originalFields) {
            //noinspection unchecked
            TableField<Record, ?>[] fields = new TableField[originalFields.length];
            for (int i = 0; i < originalFields.length; i++) {
                TableField<Record, ?> recordTableField = originalFields[i];
                fields[i] = getField(recordTableField);
            }
            return fields;
        }
    }

}
