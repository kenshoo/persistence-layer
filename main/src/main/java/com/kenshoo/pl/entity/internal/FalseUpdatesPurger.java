package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.spi.PostFetchCommandEnricher;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class FalseUpdatesPurger<E extends EntityType<E>> implements PostFetchCommandEnricher<E> {

    // Did not want to make ChangeEntityCommand.unset public so getting an accessor to call it
    private final BiConsumer<ChangeEntityCommand<E>, EntityField<E, ?>> fieldUnsetter;
    private final Set<EntityField<E, ?>> ignoredIfSetAloneFields;
    public FalseUpdatesPurger(BiConsumer<ChangeEntityCommand<E>, EntityField<E, ?>> fieldUnsetter, Stream<EntityField<E, ?>> ignoredIfSetAloneFields) {
        this.fieldUnsetter = fieldUnsetter;
        this.ignoredIfSetAloneFields = ignoredIfSetAloneFields.collect(Collectors.toSet());
    }

    @Override
    public void enrich(Collection<? extends ChangeEntityCommand<E>> commands, ChangeOperation changeOperation, ChangeContext changeContext) {
        commands.forEach(command -> {
            Entity entity = changeContext.getEntity(command);
            // Collect the fields first to avoid modification of command's inner collection while traversing
            List<FieldChange<E, ?>> unchangedFields = command.getChanges()
                    .filter(fieldChange -> areEqual(entity, fieldChange))
                    .collect(toList());
            unchangedFields.forEach(fieldChange -> fieldUnsetter.accept(command, fieldChange.getField()));
            //Contains only ignorable fields
            if(command.getChanges().allMatch(fieldChange -> ignoredIfSetAloneFields.contains(fieldChange.getField()))) {
                ignoredIfSetAloneFields.forEach(field -> fieldUnsetter.accept(command, field));
            }
        });
    }

    @Override
    public Stream<EntityField<?, ?>> getRequiredFields(Collection<? extends ChangeEntityCommand<E>> commands, ChangeOperation changeOperation) {
        return commands.stream().flatMap(ChangeEntityCommand::getChangedFields);
    }

    @Override
    public SupportedChangeOperation getSupportedChangeOperation() {
        return SupportedChangeOperation.UPDATE;
    }

    private <T> boolean areEqual(Entity entity, FieldChange<E, T> fieldChange) {
        if (!entity.containsField(fieldChange.getField())) {
            return false;
        }
        T v1 = fieldChange.getValue();
        T v2 = entity.get(fieldChange.getField());
        return fieldChange.getField().valuesEqual(v1, v2);
    }
}
