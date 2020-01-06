package com.kenshoo.pl.entity.spi.helpers;

import com.kenshoo.pl.entity.EntityChange;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;

import java.util.Arrays;
import java.util.Collection;

public class CommandsFieldMatcher {

    @SafeVarargs
    public static <E extends EntityType<E>> boolean isAnyFieldContainedInAnyCommand(final Collection<? extends EntityChange<E>> commands, final EntityField<E, ?> ... fields) {
        return commands.stream().anyMatch(cmd -> Arrays.stream(fields).anyMatch(field -> cmd.isFieldChanged(field)));
    }

    @SafeVarargs
    public static <E extends EntityType<E>> boolean isAnyFieldMissingInAnyCommand(final Collection<? extends EntityChange<E>> commands, final EntityField<E, ?> ... fields) {
        return commands.stream().anyMatch(cmd -> Arrays.stream(fields).anyMatch(field -> !cmd.isFieldChanged(field)));
    }

}
