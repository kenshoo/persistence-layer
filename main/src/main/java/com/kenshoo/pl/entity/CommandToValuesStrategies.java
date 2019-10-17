package com.kenshoo.pl.entity;

import com.kenshoo.pl.entity.internal.EntityDbUtil;

import java.util.Optional;

import static com.kenshoo.pl.entity.ChangeOperation.CREATE;

public class CommandToValuesStrategies {

    public static <E extends EntityType<E>> CommandToValuesStrategy<E> takeCommandValuesOnCreateAndContextValuesOnUpdate() {
        return (fields, cmd, ctx) -> cmd.getChangeOperation() == CREATE
                ? Optional.of(EntityDbUtil.getFieldValues(fields, cmd))
                : Optional.of(EntityDbUtil.getFieldValues(fields, ctx.getEntity(cmd)));
    }

    public static <E extends EntityType<E>> CommandToValuesStrategy<E> takeValuesFromContext() {
        return (fields, cmd, ctx) -> Optional.of(EntityDbUtil.getFieldValues(fields, ctx.getEntity(cmd)));
    }

    public static <E extends EntityType<E>> CommandToValuesStrategy<E> dontEvenTryToGetValues() {
        return (fields, cmd, ctx) -> Optional.empty();
    }
}
