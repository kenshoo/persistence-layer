package com.kenshoo.jooq;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.jooq.DataType;
import org.jooq.ForeignKey;
import org.jooq.Record;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.AbstractKeys;
import org.jooq.impl.TableImpl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class AbstractDataTable<T extends AbstractDataTable<T>> extends TableImpl<Record> implements DataTable {

    private final List<TableField<Record, ?>> primaryKeyFields = Lists.newArrayList();
    private final Supplier<UniqueKey<Record>> primaryKeySupplier = Suppliers.memoize(new PrimaryKeySupplier());
    private final Multimap<Table<Record>, FieldsPair> foreignKeyFields = ArrayListMultimap.create();
    private final Supplier<List<ForeignKey<Record, ?>>> foreignKeysSupplier = Suppliers.memoize(new ForeignKeysSupplier());

    protected AbstractDataTable(String tableName) {
        super(tableName);
    }

    protected AbstractDataTable(T aliased, String alias) {
        super(alias, null, aliased);
    }

    protected AbstractDataTable(String tableName, Schema schema) {
        super(tableName, schema);
    }

    public abstract T as(String alias);

    protected final <FT> TableField<Record, FT> createPKField(String name, DataType<FT> type) {
        TableField<Record, FT> field = createField(name, type);
        primaryKeyFields.add(field);
        return field;
    }

    protected final <FT> TableField<Record, FT> createFKField(String name, TableField<Record, FT> referenceField) {
        TableField<Record, FT> field = createField(name, referenceField.getDataType());
        foreignKeyFields.put(referenceField.getTable(), new FieldsPair(field, referenceField));
        return field;
    }

    @Override
    public Collection<FieldAndValue<?>> getVirtualPartition() {
        return Collections.emptyList();
    }

    @Override
    public UniqueKey<Record> getPrimaryKey() {
        return primaryKeySupplier.get();
    }

    @Override
    public List<ForeignKey<Record, ?>> getReferences() {
        return foreignKeysSupplier.get();
    }

    private class Keys extends AbstractKeys {
        UniqueKey<Record> createPK() {
            if(primaryKeyFields.isEmpty()) {
                return null;
            }
            //noinspection unchecked
            TableField<Record, ?>[] pkFields = primaryKeyFields.toArray(new TableField[primaryKeyFields.size()]);
            return createKey(pkFields);
        }

        UniqueKey<Record> createKey(TableField<Record, ?>[] pkFields) {
            return createUniqueKey(AbstractDataTable.this, pkFields);
        }

        ForeignKey<Record, ?> createFK(UniqueKey<Record> targetKey, Table<Record> table, TableField<Record, ?>[] fields) {
            return createForeignKey(targetKey, table, fields);
        }
    }

    private static class FieldsPair {
        final TableField<Record, ?> source;
        final TableField<Record, ?> target;
        FieldsPair(TableField<Record, ?> source, TableField<Record, ?> target) {
            this.source = source;
            this.target = target;
        }
    }

    private class PrimaryKeySupplier implements Supplier<UniqueKey<Record>> {
        @Override
        public UniqueKey<Record> get() {
            return new Keys().createPK();
        }
    }

    private class ForeignKeysSupplier implements Supplier<List<ForeignKey<Record, ?>>> {
        private final Function<FieldsPair, TableField<Record, ?>> FIELDS_PAIR_SOURCE = fieldsPair -> fieldsPair.source;
        private final Function<FieldsPair, TableField<Record, ?>> FIELDS_PAIR_TARGET = fieldsPair -> fieldsPair.target;

        @Override
        public List<ForeignKey<Record, ?>> get() {
            return Lists.newArrayList(Collections2.transform(foreignKeyFields.asMap().entrySet(), (Function<Map.Entry<Table<Record>, Collection<FieldsPair>>, ForeignKey<Record, ?>>) entry -> {
                //noinspection unchecked
                TableField<Record, ?>[] sourceFields = getTableFields(entry, FIELDS_PAIR_SOURCE);
                //noinspection unchecked
                TableField<Record, ?>[] targetFields = getTableFields(entry, FIELDS_PAIR_TARGET);
                AbstractDataTable<?> targetTable = (AbstractDataTable<?>) entry.getKey();
                UniqueKey<Record> targetKey = targetTable.new Keys().createKey(targetFields);
                return new Keys().createFK(targetKey, entry.getKey(), sourceFields);
            }));
        }

        private TableField[] getTableFields(Map.Entry<Table<Record>, Collection<FieldsPair>> entry, Function<FieldsPair, TableField<Record, ?>> extractor) {
            return Iterables.toArray(Collections2.transform(entry.getValue(), extractor), TableField.class);
        }
    }

}