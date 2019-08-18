package com.kenshoo.pl.data;

import org.apache.commons.lang3.ArrayUtils;
import org.jooq.DSLContext;
import org.jooq.ExecuteContext;
import org.jooq.ExecuteListenerProvider;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultExecuteListener;
import org.jooq.impl.SQLDataType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;


class GeneratedKeyRecorder extends DefaultExecuteListener {

    private final Field<Integer> generatedIdField;
    private final Collection<? extends CreateRecordCommand> commands;

    public DSLContext newRecordingJooq(DSLContext oldJooq) {
        DSLContext newJooq = DSL.using(oldJooq.configuration().derive());
        ExecuteListenerProvider[] originalListenerProviders = Optional.ofNullable(oldJooq.configuration().executeListenerProviders()).orElse(new ExecuteListenerProvider[0]);
        ExecuteListenerProvider[] newListsnerProviders = ArrayUtils.addAll(originalListenerProviders, () -> this);
        newJooq.configuration().set(newListsnerProviders);
        return newJooq;
    }

    public GeneratedKeyRecorder( Field<Integer> generatedIdField, Collection<? extends CreateRecordCommand> commands) {
        this.generatedIdField = generatedIdField;
        this.commands = commands;
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
            Iterator<? extends CreateRecordCommand> commandsIt = commands.iterator();
            while (generatedKeys.next() && commandsIt.hasNext()) {
                commandsIt.next().set(generatedIdField, generatedKeys.getInt("GENERATED_KEY"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
