package com.kenshoo.pl.entity;

public interface FetchWhereStep {

    /**
     * Apply the given condition to the query being built.
     *
     * @param plCondition the condition to apply, non-<code>null</code>
     * @return the final step in which the query will be executed.
     * @throws NullPointerException if the condition is <code>null</code>
     */
    FetchFinalStep where(PLCondition plCondition);
}
