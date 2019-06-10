package com.kenshoo.jooq;

import org.jooq.Query;

/**
 * See {@link SelectQueryExtender} for instructions of using this interface
 *
 * @param <Q>
 */
public interface QueryExtension<Q extends Query> extends AutoCloseable {

    Integer JOIN_TEMP_TABLE_LIMIT = 10;
    /**
     * @return the query this extension is applied to
     */
    Q getQuery();

    void close();

}
