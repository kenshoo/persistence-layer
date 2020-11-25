package com.kenshoo.pl.one2many.relatedByPK;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.spi.PostFetchCommandEnricher;
import com.kenshoo.pl.entity.spi.helpers.CommandsFieldMatcher;

import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Stream;


public class IntegerIdGeneratorEnricher<E extends EntityType<E>> implements PostFetchCommandEnricher<E> {

    private Supplier<Integer> entityIdGenerator;
    private EntityField<E, Integer> idField;

    public IntegerIdGeneratorEnricher(Supplier<Integer> entityIdGenerator, EntityField<E, Integer> idField) {
        this.entityIdGenerator = entityIdGenerator;
        this.idField = idField;
    }

    @Override
    public SupportedChangeOperation getSupportedChangeOperation() {
        return SupportedChangeOperation.CREATE;
    }

    @Override
    public void enrich(Collection<? extends ChangeEntityCommand<E>> changeEntityCommands, ChangeOperation changeOperation, ChangeContext changeContext) {
        changeEntityCommands.stream()
                .filter(command -> !command.isFieldChanged(idField))
                .forEach(command -> {
                    int id = entityIdGenerator.get();
                    command.set(idField, id);
                });
    }

    @Override
    public Stream<EntityField<E, ?>> fieldsToEnrich() {
        return Stream.of(idField);
    }

    @Override
    public boolean shouldRun(Collection<? extends EntityChange<E>> commands) {
        return CommandsFieldMatcher.isAnyFieldMissingInAnyCommand(commands, idField);
    }
}
