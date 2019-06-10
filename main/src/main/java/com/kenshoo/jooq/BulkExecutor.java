package com.kenshoo.jooq;

import org.jooq.Query;

/**
 * Created by khiloj on 3/23/16
 */
public class BulkExecutor {

    public static int updateTillNoRowsAffected(Query q) {
        int totalUpdated = 0;
        int numOfAffectedRows;

        do {
            numOfAffectedRows = q.execute();
            totalUpdated += numOfAffectedRows;
        }
        while (numOfAffectedRows != 0);
        return totalUpdated;
    }
}
