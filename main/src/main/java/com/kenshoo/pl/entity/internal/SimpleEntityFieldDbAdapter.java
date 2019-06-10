package com.kenshoo.pl.entity.internal;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.EntityFieldDbAdapter;
import com.kenshoo.pl.entity.ValueConverter;
import org.jooq.Record;
import org.jooq.TableField;

import java.util.Iterator;
import java.util.stream.Stream;

public class SimpleEntityFieldDbAdapter<T, DBT> implements EntityFieldDbAdapter<T> {

    private final DataTable table;
    private final TableField<Record, DBT> tableField;
    private final ValueConverter<T, DBT> valueConverter;

    public SimpleEntityFieldDbAdapter(TableField<Record, DBT> tableField, ValueConverter<T, DBT> valueConverter) {
        this.table = (DataTable) tableField.getTable();
        this.tableField = tableField;
        this.valueConverter = valueConverter;
    }

    @Override
    public DataTable getTable() {
        return table;
    }

    @Override
    public Stream<TableField<Record, ?>> getTableFields() {
        return Stream.of(tableField);
    }

    @Override
    public Stream<Object> getDbValues(T value) {
        return Stream.of(valueConverter.convertTo(value));
    }

    @Override
    public T getFromRecord(Iterator<Object> valuesIterator) {
        //noinspection unchecked
        return valueConverter.convertFrom((DBT) valuesIterator.next());
    }
}
