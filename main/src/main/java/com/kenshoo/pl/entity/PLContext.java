package com.kenshoo.pl.entity;

import com.kenshoo.pl.entity.internal.EntitiesFetcher;
import com.kenshoo.pl.entity.spi.PersistenceLayerRetryer;
import org.jooq.DSLContext;
import org.jooq.lambda.Seq;

import java.util.function.Predicate;

import static com.kenshoo.pl.entity.spi.PersistenceLayerRetryer.JUST_RUN_WITHOUT_CHECKING_DEADLOCKS;


public class PLContext {

    final private DSLContext dslContext;
    final private PersistenceLayerRetryer retryer;
    final private Predicate<Feature> featurePredicate;

    private PLContext(DSLContext dslContext, PersistenceLayerRetryer retryer, Predicate<Feature> featurePredicate) {
        this.dslContext = dslContext;
        this.retryer = retryer;
        this.featurePredicate = featurePredicate;
    }

    public DSLContext dslContext() {
        return dslContext;
    }

    public FeatureSet generateFeatureSet() {
        return new FeatureSet(Seq.of(Feature.values()).filter(featurePredicate));
    }

    public PersistenceLayerRetryer persistenceLayerRetryer() {
        return  retryer;
    }

    /**
     * Start building a query to fetch entities with the given fields.
     *
     * @param fields the fields to fetch, must not be empty
     * @return the next step in which the entity type will be specified
     */
    public FetchFromStep select(final EntityField<?, ?>... fields) {
        return new FluentEntitiesFetcher(fetcher(), fields);
    }

    private EntitiesFetcher fetcher() {
        return new EntitiesFetcher(dslContext, generateFeatureSet());
    }

    public static class Builder {

        private DSLContext dslContext;
        private PersistenceLayerRetryer retryer = JUST_RUN_WITHOUT_CHECKING_DEADLOCKS;
        private Predicate<Feature> featurePredicate = __ -> false;

        public Builder withFeaturePredicate(Predicate<Feature> featurePredicate) {
            this.featurePredicate = featurePredicate;
            return this;
        }

        public Builder(DSLContext dslContext) {
            this.dslContext = dslContext;
        }

        public Builder withRetryer(PersistenceLayerRetryer retryer) {
            this.retryer = retryer;
            return this;
        }

        public PLContext build() {
            return new PLContext(dslContext, retryer, featurePredicate);
        }

    }
}
