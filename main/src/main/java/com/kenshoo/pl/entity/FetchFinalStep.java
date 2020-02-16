package com.kenshoo.pl.entity;

import java.util.List;

public interface FetchFinalStep {

    /**
     * Finish building the query and fetch the results from the DB
     *
     * @return the result entities
     */
    List<Entity> fetch();
}
