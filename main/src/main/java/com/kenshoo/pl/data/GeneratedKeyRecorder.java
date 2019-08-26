package com.kenshoo.pl.data;

import org.apache.commons.lang3.ArrayUtils;
import org.jooq.DSLContext;
import org.jooq.ExecuteContext;
import org.jooq.ExecuteListenerProvider;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultExecuteListener;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;


class GeneratedKeyRecorder extends DefaultExecuteListener {

    private final Field generatedIdField;
    private final List<Object> generatedKeys;

    public DSLContext newRecordingJooq(DSLContext oldJooq) {
        DSLContext newJooq = DSL.using(oldJooq.configuration().derive());
        ExecuteListenerProvider[] originalListenerProviders = Optional.ofNullable(oldJooq.configuration().executeListenerProviders()).orElse(new ExecuteListenerProvider[0]);
        ExecuteListenerProvider[] newListenerProviders = ArrayUtils.addAll(originalListenerProviders, () -> this);
        newJooq.configuration().set(newListenerProviders);
        return newJooq;
    }

    public GeneratedKeyRecorder(Field<?> generatedIdField, int capacity) {
        this.generatedIdField = generatedIdField;
        this.generatedKeys = new ArrayList<>(capacity);
    }

    public List<Object> getGeneratedKeys() {
        return generatedKeys;
    }

    @Override
    public void prepareEnd(ExecuteContext ctx) {
        try {
            PreparedStatement preparedStatement = ctx.connection().prepareStatement(ctx.sql(), Statement.RETURN_GENERATED_KEYS);
            ctx.statement(preparedStatement);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void executeEnd(ExecuteContext ctx) {
        try (ResultSet fromDB = ctx.statement().getGeneratedKeys()) {
            while (fromDB.next()) {
                generatedKeys.add(fromDB.getObject("GENERATED_KEY", generatedIdField.getType()));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
