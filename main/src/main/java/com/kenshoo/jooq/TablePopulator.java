package com.kenshoo.jooq;

import org.jooq.BatchBindStep;

public interface TablePopulator {

    void populate(BatchBindStep batchBindStep);
}
