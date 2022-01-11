package com.kenshoo.pl.entity.spi;


import com.kenshoo.pl.entity.ChangeContext;
import com.kenshoo.pl.entity.ChangeEntityCommand;
import com.kenshoo.pl.entity.EntityType;

public interface PostFetchCommandEnrichmentListener<E extends EntityType<E>, Event extends EnrichmentEvent> {

    Class<Event> getEventType();

    void enrich(ChangeEntityCommand<E> commandToEnrich, Event enrichmentEvent, ChangeContext changeContext);
}
