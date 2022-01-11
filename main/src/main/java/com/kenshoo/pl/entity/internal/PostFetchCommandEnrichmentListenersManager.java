package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.ChangeContext;
import com.kenshoo.pl.entity.ChangeEntityCommand;
import com.kenshoo.pl.entity.ChangeFlowConfig;
import com.kenshoo.pl.entity.EntityType;

import com.kenshoo.pl.entity.spi.EnrichmentEvent;
import com.kenshoo.pl.entity.spi.PostFetchCommandEnrichmentListener;
import com.kenshoo.pl.entity.spi.PostFetchCommandEnricher;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.kenshoo.pl.entity.internal.EntityCommandUtil.getAncestor;

public class PostFetchCommandEnrichmentListenersManager {

    private final List<PostFetchCommandEnrichmentListenerDelegate> listenerDelegates = new ArrayList<>();

    public void publish(EnrichmentEvent enrichmentEvent, ChangeContext changeContext) {
        listenerDelegates.stream().
                filter(l -> Objects.equals(l.getEventType(), enrichmentEvent.getClass())).
                forEach(l -> l.enrich(getAncestor(enrichmentEvent.getSource(), l.getEntityType()), enrichmentEvent, changeContext));
    }

    public static <E extends EntityType<E>> PostFetchCommandEnrichmentListenersManager build(ChangeFlowConfig<E> flowConfig) {
        PostFetchCommandEnrichmentListenersManager listenersManager = new PostFetchCommandEnrichmentListenersManager();
        buildRecursive(flowConfig, listenersManager);
        return listenersManager;
    }

    private static <E extends EntityType<E>> void buildRecursive(ChangeFlowConfig<E> flowConfig, PostFetchCommandEnrichmentListenersManager listenersManager) {
        buildOneLayer(listenersManager, flowConfig.getEntityType(), flowConfig.getPostFetchCommandEnrichers());
        flowConfig.childFlows().forEach(flow -> buildRecursive(flow, listenersManager));
    }

    static private <E extends EntityType<E>> void buildOneLayer(PostFetchCommandEnrichmentListenersManager listenersManager, E entityType, List<PostFetchCommandEnricher<E>> postFetchCommandEnrichers) {
        postFetchCommandEnrichers.stream().
                filter(e -> e instanceof PostFetchCommandEnrichmentListener).
                map(e -> new PostFetchCommandEnrichmentListenerDelegate<>(entityType, (PostFetchCommandEnrichmentListener) e)).
                forEach(e -> listenersManager.listenerDelegates.add(0, e));

    }

    private static class PostFetchCommandEnrichmentListenerDelegate<E extends EntityType<E>, Event extends EnrichmentEvent> implements PostFetchCommandEnrichmentListener<E, Event> {

        private final E entityType;
        private final PostFetchCommandEnrichmentListener<E, Event> listener;

        public PostFetchCommandEnrichmentListenerDelegate(E entityType, PostFetchCommandEnrichmentListener<E, Event> listener) {
            this.entityType = entityType;
            this.listener = listener;
        }

        public E getEntityType() {
            return entityType;
        }

        @Override
        public Class<Event> getEventType() {
            return listener.getEventType();
        }

        @Override
        public void enrich(ChangeEntityCommand<E> commandToEnrich, Event enrichmentEvent, ChangeContext changeContext) {
            listener.enrich(commandToEnrich, enrichmentEvent, changeContext);
        }
    }

}
