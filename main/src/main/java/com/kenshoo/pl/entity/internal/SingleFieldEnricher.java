package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.spi.PostFetchCommandEnricher;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Stream;

abstract public class SingleFieldEnricher<E extends EntityType<E>, T> implements PostFetchCommandEnricher<E> {

    @Override
    final public void enrich(Collection<? extends ChangeEntityCommand<E>> changeEntityCommands, ChangeOperation changeOperation, ChangeContext changeContext) {
        changeEntityCommands.stream()
                .filter(commandFilter())
                .filter(command -> shouldRunForCommand(command) && needEnrich(command, changeContext.getEntity(command)))
                .forEach(command -> command.set(enrichedField(), enrichedValue(command, changeContext.getEntity(command))));
    }

    @Override
    final public Stream<EntityField<E, ?>> fieldsToEnrich() {
        return Stream.of(enrichedField());
    }

    @Override
    final public boolean shouldRun(Collection<? extends EntityChange<E>> commands) {
        return commands.stream().filter(commandFilter()).anyMatch(this::shouldRunForCommand);
    }

    abstract protected EntityField<E, T> enrichedField();

    abstract protected T enrichedValue(EntityChange<E> entityChange, Entity entity);

    protected boolean needEnrich(EntityChange<E> entityChange, Entity entity) {
        return true;
    }

    protected Predicate<EntityChange<E>> commandFilter() {
        return entityChange -> true;
    }

    protected boolean considerNullAsMissing() {
        return false;
    }

    protected boolean shouldRunForCommand(EntityChange<E> entityChange) {
        return shouldEnrichField(entityChange);
    }

    private boolean shouldEnrichField(EntityChange<E> entityChange) {
        return enrichedFieldIsMissing(entityChange) || (considerNullAsMissing() && enrichedFieldHasNullValue(entityChange));
    }

    private boolean enrichedFieldHasNullValue(EntityChange<E> entityChange) {
        return entityChange.get(enrichedField()) == null;
    }

    private boolean enrichedFieldIsMissing(EntityChange<E> entityChange) {
        return !entityChange.isFieldChanged(enrichedField());
    }
}
