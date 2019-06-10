package com.kenshoo.pl.entity.internal;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.EntityFieldDbAdapter;
import org.jooq.Record;
import org.jooq.TableField;

import java.util.Iterator;
import java.util.stream.Stream;

public class EmptyVirtualEntityFieldDbAdapter<T, T1> implements EntityFieldDbAdapter<T> {

    private final DataTable table;

    public EmptyVirtualEntityFieldDbAdapter(DataTable table) {
        this.table = table;
    }

    @Override
    public DataTable getTable() {
        return table;
    }

    @Override
    public Stream<TableField<Record, ?>> getTableFields() {
        return Stream.empty();
    }

    @Override
    public Stream<Object> getDbValues(T value) {
        throw new IllegalStateException("Virtual fields can not be written to the database");
    }

    @Override
    public T getFromRecord(Iterator<Object> valuesIterator) {
        return null;
    }
}
