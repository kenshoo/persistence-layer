package com.kenshoo.pl.entity;

import java.util.Collection;
import java.util.List;

public interface FetchFinalStep {

    /**
     * Finish building the query and fetch the results from the DB
     *
     * @return the result entities
     */
    List<CurrentEntityState> fetch();

    /**
     * Finish building the query and fetch the results from the DB
     * @param keys the keys to fetch
     * @return the result entities
     */
    List<CurrentEntityState> fetchByKeys(Collection<? extends Identifier<?>> keys);
}
