package com.kenshoo.pl.entity.spi;

import com.google.common.base.Throwables;

public interface PersistenceLayerRetryer {

    void run(ThrowingAction action);

    PersistenceLayerRetryer JUST_RUN_WITHOUT_CHECKING_DEADLOCKS = action -> {
        try {
            action.run();
        } catch (Exception e) {
            Throwables.propagate(e);
        }
    };

}
