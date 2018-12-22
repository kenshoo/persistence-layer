package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.jooq.DataTable;
import com.kenshoo.pl.entity.EntityFieldDbAdapter;
import org.jooq.Record;
import org.jooq.TableField;

import java.util.Iterator;
import java.util.function.Function;
import java.util.stream.Stream;

public class VirtualEntityFieldDbAdapter<T, T1> implements EntityFieldDbAdapter<T> {

    private final EntityFieldDbAdapter<T1> adapter1;
    private final Function<T1, T> translationFunction;

    public VirtualEntityFieldDbAdapter(EntityFieldDbAdapter<T1> adapter1, Function<T1, T> translationFunction) {
        this.adapter1 = adapter1;
        this.translationFunction = translationFunction;
    }

    @Override
    public DataTable getTable() {
        return adapter1.getTable();
    }

    @Override
    public Stream<TableField<Record, ?>> getTableFields() {
        return adapter1.getTableFields();
    }

    @Override
    public Stream<Object> getDbValues(T value) {
        throw new IllegalStateException("Virtual fields can not be written to the database");
    }

    @Override
    public T getFromRecord(Iterator<Object> valuesIterator) {
        return translationFunction.apply(adapter1.getFromRecord(valuesIterator));
    }
}
