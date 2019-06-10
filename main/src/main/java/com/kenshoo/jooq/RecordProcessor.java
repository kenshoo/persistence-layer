package com.kenshoo.jooq;

public interface RecordProcessor<C extends Cursor> {

    void process(C cursor);
}
