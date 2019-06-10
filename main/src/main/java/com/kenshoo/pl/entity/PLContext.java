package com.kenshoo.pl.entity;

import com.kenshoo.pl.entity.spi.PersistenceLayerRetryer;
import org.jooq.DSLContext;

import static com.kenshoo.pl.entity.spi.PersistenceLayerRetryer.JUST_RUN_WITHOUT_CHECKING_DEADLOCKS;

public class PLContext {

    final private DSLContext dslContext;

    final private PersistenceLayerRetryer retryer;

    private PLContext(DSLContext dslContext, PersistenceLayerRetryer retryer) {
        this.dslContext = dslContext;
        this.retryer = retryer;
    }

    public DSLContext dslContext() {
        return dslContext;
    }

    public PersistenceLayerRetryer persistenceLayerRetryer() {
        return  retryer;
    }

    public static class Builder {

        private DSLContext dslContext;
        private PersistenceLayerRetryer retryer = JUST_RUN_WITHOUT_CHECKING_DEADLOCKS;

        public Builder(DSLContext dslContext) {
            this.dslContext = dslContext;
        }

        public Builder withRetryer(PersistenceLayerRetryer retryer) {
            this.retryer = retryer;
            return this;
        }

        public PLContext build() {
            return new PLContext(dslContext, retryer);
        }

    }
}
