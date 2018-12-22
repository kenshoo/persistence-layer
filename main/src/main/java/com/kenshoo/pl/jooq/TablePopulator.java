package com.kenshoo.pl.jooq;

import org.jooq.BatchBindStep;

public interface TablePopulator {

    void populate(BatchBindStep batchBindStep);
}
