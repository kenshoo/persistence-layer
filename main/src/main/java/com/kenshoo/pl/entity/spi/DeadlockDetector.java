package com.kenshoo.pl.entity.spi;

public interface DeadlockDetector {
    boolean isDeadlock(Throwable e);
}
