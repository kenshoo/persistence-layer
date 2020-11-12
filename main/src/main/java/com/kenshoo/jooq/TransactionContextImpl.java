package com.kenshoo.jooq;

import org.jooq.*;
import org.jooq.conf.Settings;

import java.util.Map;

public class TransactionContextImpl implements TransactionContext {

    private final Configuration config;
    private final DSLContext dslContext;
    private Transaction transaction;
    private Throwable cause;

    public TransactionContextImpl(final Configuration config, final DSLContext dslContext) {
        this.config = config;
        // As of Jooq 3.10.x the DSLContext can be obtained from the Configuration, but we want to be backwards-compatible to 3.9.x
        this.dslContext = dslContext;
    }

    @Override
    public final Transaction transaction() {
        return transaction;
    }

    @Override
    public final TransactionContext transaction(Transaction t) {
        transaction = t;
        return this;
    }

    @Override
    public final Exception cause() {
        return cause instanceof Exception ? (Exception) cause : null;
    }

    /**
     * This was introduced in Jooq 3.10. For backwards compatibility of our code omitting the "@Override"
     */
    public Throwable causeThrowable() {
        return cause;
    }

    @Override
    public final TransactionContext cause(Exception c) {
        cause = c;
        return this;
    }

    /**
     * This was introduced in Jooq 3.10. For backwards compatibility of our code omitting the "@Override"
     */
    public TransactionContext causeThrowable(final Throwable cause) {
        this.cause = cause;
        return this;
    }

    @Override
    public Configuration configuration() {
        return config;
    }

    /**
     * This was introduced in Jooq 3.10. For backwards compatibility of our code omitting the "@Override"
     */
    public DSLContext dsl() {
        return dslContext;
    }

    @Override
    public Settings settings() {
        return config.settings();
    }

    @Override
    public SQLDialect dialect() {
        return config.dialect();
    }

    @Override
    public SQLDialect family() {
        return config.family();
    }

    @Override
    public Map<Object, Object> data() {
        return config.data();
    }

    @Override
    public Object data(Object key) {
        return config.data(key);
    }

    @Override
    public Object data(Object key, Object value) {
        return config.data(key, value);
    }
}
