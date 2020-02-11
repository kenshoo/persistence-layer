package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.spi.PostFetchCommandEnricher;

import java.util.Collection;
import java.util.stream.Stream;

abstract public class SingleFieldEnricher<E extends EntityType<E>, T> implements PostFetchCommandEnricher<E> {

    @Override
    final public void enrich(Collection<? extends ChangeEntityCommand<E>> changeEntityCommands, ChangeOperation changeOperation, ChangeContext changeContext) {
        changeEntityCommands.stream()
                .filter(command -> shouldRunForCommand(command) && needEnrich(command, changeContext.getEntity(command)))
                .forEach(command -> command.set(enrichedField(), enrichedValue(command, changeContext.getEntity(command))));
    }

    @Override
    final public Stream<EntityField<E, ?>> fieldsToEnrich() {
        return Stream.of(enrichedField());
    }

    @Override
    final public boolean shouldRun(Collection<? extends EntityChange<E>> commands) {
        return commands.stream().anyMatch(this::shouldRunForCommand);
    }

    abstract protected EntityField<E, T> enrichedField();

    abstract protected T enrichedValue(EntityChange<E> entityChange, Entity entity);

    protected boolean needEnrich(EntityChange<E> entityChange, Entity entity) {
        return true;
    }

    protected Stream<EntityField<E, ?>> triggeredByFields() {
        return Stream.empty();
    }

    protected boolean considerNullAsMissing() {
        return false;
    }

    protected boolean shouldRunForCommand(EntityChange<E> entityChange) {
        return shouldEnrichField(entityChange) && (triggeredFieldIsNotRequested() || hasAnyTriggeredField(entityChange));
    }

    private boolean shouldEnrichField(EntityChange<E> entityChange) {
        return enrichedFieldIsMissing(entityChange) || (considerNullAsMissing() && enrichedFieldHasNullValue(entityChange));
    }

    private boolean enrichedFieldHasNullValue(EntityChange<E> entityChange) {
        return entityChange.get(enrichedField()) == null;
    }

    private boolean triggeredFieldIsNotRequested() {
        return triggeredByFields().count() == 0;
    }

    private boolean hasAnyTriggeredField(EntityChange<E> entityChange) {
        return triggeredByFields().anyMatch(entityChange::isFieldChanged);
    }

    private boolean enrichedFieldIsMissing(EntityChange<E> entityChange) {
        return !entityChange.isFieldChanged(enrichedField());
    }
}
