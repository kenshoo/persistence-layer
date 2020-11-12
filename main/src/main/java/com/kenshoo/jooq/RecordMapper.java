package com.kenshoo.jooq;

public interface RecordMapper<C extends Cursor, T> {

    T map(C cursor);
}
