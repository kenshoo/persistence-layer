package com.kenshoo.pl.jooq;

import org.jooq.ResultQuery;

/**
 * See {@link QueryExtender} for instructions of using this interface
 *
 * @param <Q>
 */
public interface QueryExtension<Q extends ResultQuery> extends AutoCloseable {

    /**
     * @return the query this extension is applied to
     */
    Q getQuery();

    void close();

}
