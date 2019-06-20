package com.kenshoo.pl.entity;

import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;
import static org.jooq.lambda.Seq.seq;

public class ChangeResult<E extends EntityType<E>, ID extends Identifier<E>, C extends ChangeEntityCommand<E>> implements Iterable<EntityChangeResult<E, ID, C>> {

    private final Collection<EntityChangeResult<E, ID, C>> changeResults;
    private final Map<ChangeEntityCommand<E>, EntityChangeResult<E, ID, C>> resultsAsMap;
    private final PersistentLayerStats stats;

    public ChangeResult(Iterable<? extends EntityChangeResult<E, ID, C>> changeResults, PersistentLayerStats stats) {
        this.changeResults = ImmutableList.copyOf(changeResults);
        resultsAsMap = seq(this.changeResults).collect(toMap(EntityChangeResult::getCommand, Function.identity()));
        this.stats = stats;
    }

    public boolean hasErrors() {
        return changeResults.stream().anyMatch(changeResult -> !changeResult.getErrors().isEmpty());
    }

    public boolean hasErrors(C command) {
        return !getErrors(command).isEmpty();
    }

    public Collection<ValidationError> getErrors(C command) {
        return resultsAsMap.get(command).getErrors();
    }

    @Override
    public Iterator<EntityChangeResult<E, ID, C>> iterator() {
        return changeResults.iterator();
    }

    public Collection<EntityChangeResult<E, ID, C>> getChangeResults() {
        return changeResults;
    }

    public PersistentLayerStats getStats() {
        return stats;
    }
}
