package com.kenshoo.jooq;

import org.jooq.Configuration;
import org.jooq.SQLDialect;
import org.jooq.Transaction;
import org.jooq.TransactionContext;
import org.jooq.conf.Settings;

import java.util.Map;

public class TransactionContextImpl implements TransactionContext {

    private final Configuration config;
    private Transaction transaction;
    private Exception cause;

    public TransactionContextImpl(Configuration config) {
        this.config = config;
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
        return cause;
    }

    @Override
    public final TransactionContext cause(Exception c) {
        cause = c;
        return this;
    }

    @Override
    public Configuration configuration() {
        return config;
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
