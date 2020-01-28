package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.spi.PostFetchCommandEnricher;

import java.util.Collection;
import java.util.stream.Stream;

abstract public class SingleFieldEnricher<E extends EntityType<E>, T> implements PostFetchCommandEnricher<E> {

    @Override
    public void enrich(Collection<? extends ChangeEntityCommand<E>> changeEntityCommands, ChangeOperation changeOperation, ChangeContext changeContext) {
        changeEntityCommands.stream()
                .filter(command -> shouldRunForCommand(command) && needEnrich(command, changeContext.getEntity(command)))
                .forEach(command -> command.set(enrichedField(), enrichedValue(command, changeContext.getEntity(command))));
    }

    @Override
    public Stream<EntityField<E, ?>> fieldsToEnrich() {
        return Stream.of(enrichedField());
    }

    @Override
    public boolean shouldRun(Collection<? extends ChangeEntityCommand<E>> commands) {
        return commands.stream().anyMatch(this::shouldRunForCommand);
    }

    abstract protected EntityField<E, T> enrichedField();

    abstract protected T enrichedValue(EntityChange<E> entityChange, Entity entity);

    protected boolean needEnrich(EntityChange<E> entityChange, Entity entity) {
        return true;
    }

    protected boolean shouldRunForCommand(ChangeEntityCommand<E> command) {
        return !command.isFieldChanged(enrichedField());
    }
}
