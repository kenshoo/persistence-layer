package com.kenshoo.jooq;

import org.jooq.ConnectionProvider;

public interface ClosableConnectionProvider extends ConnectionProvider, AutoCloseable {
}
