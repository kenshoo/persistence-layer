package com.kenshoo.jooq;

import org.jooq.Record;
import org.jooq.Table;

public interface TempTableResource<T extends Table<Record>> extends AutoCloseable {

    T getTable();

    @Override
    void close();
}
