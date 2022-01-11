package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.spi.EnrichmentEvent;

public interface PostFetchCommandEnrichmentPublisher {

    void publish(EnrichmentEvent enrichmentEvent, ChangeContext changeContext);
}
