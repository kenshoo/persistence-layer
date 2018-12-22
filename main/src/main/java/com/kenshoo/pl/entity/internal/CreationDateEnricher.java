package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.spi.PostFetchCommandEnricher;

import java.time.Instant;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Created by yuvalr on 2/3/16.
 */
public class CreationDateEnricher<E extends EntityType<E>> implements PostFetchCommandEnricher<E> {

    private final EntityField<E, Instant> creationDateField;

    public CreationDateEnricher(EntityField<E, Instant> creationDateField) {
        this.creationDateField = creationDateField;
    }

    @Override
    public void enrich(Collection<? extends ChangeEntityCommand<E>> changeEntityCommands, ChangeOperation changeOperation, ChangeContext changeContext) {
        Instant now = Instant.now();
        changeEntityCommands.stream().forEach(command -> command.set(creationDateField, now));
    }

    @Override
    public SupportedChangeOperation getSupportedChangeOperation() {
        return SupportedChangeOperation.CREATE;
    }

    @Override
    public Stream<EntityField<?, ?>> getRequiredFields(Collection<? extends ChangeEntityCommand<E>> changeEntityCommands, ChangeOperation changeOperation) {
        return Stream.empty();
    }
}
