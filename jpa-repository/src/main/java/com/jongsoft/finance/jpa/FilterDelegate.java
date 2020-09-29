package com.jongsoft.finance.jpa;

import io.micronaut.data.model.Sort;

import javax.persistence.Query;

public interface FilterDelegate<T extends FilterDelegate> {

    /**
     * Generates the HQL query that belongs to the command supported by the delegate.
     *
     * @return string with HQL query, starting with the 'from' part
     */
    String generateHql();

    /**
     * Create the sorting for the query.
     *
     * @return the sort
     */
    Sort sort();

    /**
     * Append a filtering for user.
     *
     * @param username
     */
    T user(String username);

    /**
     * Prepare the query based upon the filter command supported by the delegate.
     *
     * @param query
     */
    @Deprecated
    T prepareQuery(Query query);

}
