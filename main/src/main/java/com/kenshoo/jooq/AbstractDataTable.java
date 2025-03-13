package com.kenshoo.jooq;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.*;
import org.jooq.Table;
import org.jooq.*;
import org.jooq.impl.TableImpl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.jooq.impl.Internal.*;

public abstract class AbstractDataTable<T extends AbstractDataTable<T>> extends TableImpl<Record> implements DataTable {

    private final List<TableField<Record, ?>> primaryKeyFields = Lists.newArrayList();
    private final Supplier<UniqueKey<Record>> primaryKeySupplier = Suppliers.memoize(new PrimaryKeySupplier());
    private final Multimap<Table<Record>, FieldsPair> foreignKeyFields = ArrayListMultimap.create();
    private final Supplier<List<ForeignKey<Record, ?>>> foreignKeysSupplier = Suppliers.memoize(new ForeignKeysSupplier());
    private final Supplier<Identity<Record, ?>> identitySupplier = Suppliers.memoize(new IdentitySupplier());
    private TableField<Record, ?> identityField;

    protected AbstractDataTable(String tableName) {
        super(tableName);
    }

    protected AbstractDataTable(T aliased, String alias) {
        super(alias, null, aliased);
    }

    protected AbstractDataTable(T aliased, Schema schema, String alias) {
        super(alias, schema, aliased);
    }

    protected AbstractDataTable(String tableName, Schema schema) {
        super(tableName, schema);
    }

    public abstract T as(String alias);

    protected final <FT> TableField<Record, FT> createPKField(String name, DataType<FT> type) {
        TableField<Record, FT> field = createField(name, type);
        primaryKeyFields.add(field);
        if(type.identity()) {
            this.identityField = field;
        }
        return field;
    }

    protected final <FT> TableField<Record, FT> createPKAndFKField(String name, DataType<FT> type, TableField<Record, FT> referenceField) {
        final TableField<Record, FT> field = createPKField(name, type);
        foreignKeyFields.put(referenceField.getTable(), new FieldsPair(field, referenceField));
        return field;
    }

    protected final <FT> TableField<Record, FT> createFKField(String name, TableField<Record, FT> referenceField) {
        TableField<Record, FT> field = createField(name, referenceField.getDataType().identity(false));
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

    @Override
    public Identity<Record, ?> getIdentity() {
        return identitySupplier.get();
    }

    private UniqueKey<Record> createPK() {
        if(primaryKeyFields.isEmpty()) {
            return null;
        }
        //noinspection unchecked
        TableField<Record, ?>[] pkFields = primaryKeyFields.toArray(new TableField[primaryKeyFields.size()]);
        return createKey(pkFields);
    }

    private UniqueKey<Record> createKey(TableField<Record, ?>[] pkFields) {
        return createUniqueKey(AbstractDataTable.this, pkFields);
    }

   private ForeignKey<Record, ?> createFK(UniqueKey<Record> targetKey, Table<Record> table, TableField<Record, ?>[] fields) {
        return createForeignKey(targetKey, table, fields);
    }

    private Identity<Record, ?> createPKIdentity() {
        return identityField != null ? createIdentity(AbstractDataTable.this, identityField) : null;
    }

    private static class FieldsPair {
        final TableField<Record, ?> source;
        final TableField<Record, ?> target;
        FieldsPair(TableField<Record, ?> source, TableField<Record, ?> target) {
            this.source = source;
            this.target = target;
        }
    }

    private class IdentitySupplier implements Supplier<Identity<Record, ?>> {
        @Override
        public Identity<Record, ?> get() {
            return createPKIdentity();
        }
    }

    private class PrimaryKeySupplier implements Supplier<UniqueKey<Record>> {
        @Override
        public UniqueKey<Record> get() {
            return createPK();
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
                UniqueKey<Record> targetKey = targetTable.createKey(targetFields);
                return createFK(targetKey, entry.getKey(), sourceFields);
            }));
        }

        private TableField[] getTableFields(Map.Entry<Table<Record>, Collection<FieldsPair>> entry, Function<FieldsPair, TableField<Record, ?>> extractor) {
            return Iterables.toArray(Collections2.transform(entry.getValue(), extractor), TableField.class);
        }
    }

}
