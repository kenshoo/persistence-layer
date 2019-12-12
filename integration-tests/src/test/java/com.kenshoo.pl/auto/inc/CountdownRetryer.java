package com.kenshoo.pl.auto.inc;

import com.kenshoo.pl.entity.spi.PersistenceLayerRetryer;
import com.kenshoo.pl.entity.spi.ThrowingAction;

public class CountdownRetryer implements PersistenceLayerRetryer {

    int countdown;

    public CountdownRetryer(int numOfRetries) {
        this.countdown = numOfRetries;
    }

    @Override
    public void run(ThrowingAction action) {
        while (countdown-- > 0) {
            try {
                System.out.println("Retryer is trying");
                action.run();
                System.out.println("Retryer finished successfully");
                break;
            } catch (Throwable error) {
                System.out.println("Retryer caught an error");
            }
        }
    }
}
