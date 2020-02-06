package com.kenshoo.pl.entity.internal;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.EntityFieldDbAdapter;
import org.jooq.Record;
import org.jooq.TableField;

import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public class VirtualEntityFieldDbAdapter2<T, T1, T2> implements EntityFieldDbAdapter<T> {

    private final EntityFieldDbAdapter<T1> adapter1;
    private final EntityFieldDbAdapter<T2> adapter2;
    private final BiFunction<T1, T2, T> combiningFunction;

    public VirtualEntityFieldDbAdapter2(EntityFieldDbAdapter<T1> adapter1, EntityFieldDbAdapter<T2> adapter2, BiFunction<T1, T2, T> combiningFunction) {
        this.combiningFunction = combiningFunction;
        this.adapter1 = adapter1;
        this.adapter2 = adapter2;
    }

    @Override
    public DataTable getTable() {
        return adapter1.getTable();
    }

    @Override
    public Stream<TableField<Record, ?>> getTableFields() {
        return Stream.of(adapter1, adapter2).flatMap(EntityFieldDbAdapter::getTableFields);
    }

    @Override
    public Stream<Object> getDbValues(T value) {
        throw new IllegalStateException("Virtual fields can not be written to the database");
    }

    @Override
    public Object getFirstDbValue(T value) {
        throw new IllegalStateException("Virtual fields can not be written to the database");
    }

    @Override
    public T getFromRecord(Iterator<Object> valuesIterator) {
        return combiningFunction.apply(adapter1.getFromRecord(valuesIterator), adapter2.getFromRecord(valuesIterator));
    }
}
