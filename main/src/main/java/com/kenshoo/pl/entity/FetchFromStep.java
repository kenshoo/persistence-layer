package com.kenshoo.pl.entity;

public interface FetchFromStep {

    /**
     * Add the primary entity type to the query being built.<br>
     * This entity will determine the table that the query will start with.<br>
     * If additional tables are needed for the requested fields, corresponding joins will be automatically appended to the query.
     *
     * @param entityType the primary entity type to fetch from, non-<code></code>null</code>
     * @return the next step in which a condition will be added to the query
     * @throws NullPointerException if the entity is <code>null</code>
     */
    FetchWhereStep from(EntityType<?> entityType);
}
