package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.spi.PostFetchCommandEnricher;

import java.util.Collection;
import java.util.stream.Stream;

abstract public class SimpleFieldEnricher<E extends EntityType<E>, T> implements PostFetchCommandEnricher<E> {

    @Override
    public void enrich(Collection<? extends ChangeEntityCommand<E>> changeEntityCommands, ChangeOperation changeOperation, ChangeContext changeContext) {
        changeEntityCommands.stream()
                .filter(this::needEnrich)
                .forEach(command -> command.set(field(), fieldValue(command, changeContext.getEntity(command))));
    }

    @Override
    public Stream<EntityField<E, ?>> fieldsToEnrich() {
        return Stream.of(field());
    }

    @Override
    public boolean shouldRun(Collection<? extends ChangeEntityCommand<E>> commands) {
        return commands.stream().anyMatch(this::needEnrich);
    }

    abstract protected EntityField<E, T> field();

    abstract protected T fieldValue(EntityChange<E> entityChange, Entity entity);

    protected boolean needEnrich(ChangeEntityCommand<E> command) {
        return !command.isFieldChanged(field());
    }
}
