package com.kenshoo.pl.data;

import org.apache.commons.lang3.ArrayUtils;
import org.jooq.DSLContext;
import org.jooq.ExecuteContext;
import org.jooq.ExecuteListenerProvider;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultExecuteListener;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


class GeneratedKeyRecorder extends DefaultExecuteListener {

    private final List<Long> generatedKeys;

    public List<Long> getGeneratedKeys() {
        return generatedKeys;
    }

    public DSLContext newRecordingJooq(DSLContext oldJooq) {
        DSLContext newJooq = DSL.using(oldJooq.configuration().derive());
        ExecuteListenerProvider[] originalListenerProviders = Optional.ofNullable(oldJooq.configuration().executeListenerProviders()).orElse(new ExecuteListenerProvider[0]);
        ExecuteListenerProvider[] newListsnerProviders = ArrayUtils.addAll(originalListenerProviders, () -> this);
        newJooq.configuration().set(newListsnerProviders);
        return newJooq;
    }

    public GeneratedKeyRecorder(int numOfCommands) {
        this.generatedKeys = new ArrayList<>(numOfCommands);
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
        try {
            final ResultSet generatedKeys = ctx.statement().getGeneratedKeys();
            while (generatedKeys.next()) {
                this.generatedKeys.add(generatedKeys.getLong("GENERATED_KEY"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
