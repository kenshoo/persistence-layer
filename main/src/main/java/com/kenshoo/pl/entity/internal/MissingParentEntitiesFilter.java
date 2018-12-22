package com.kenshoo.pl.entity.internal;

import com.google.common.collect.Collections2;
import com.kenshoo.pl.entity.*;

import java.util.Collection;
import java.util.stream.Stream;

public class MissingParentEntitiesFilter<E extends EntityType<E>> implements ChangesFilter<E> {

    private final Collection<EntityField<E, ?>> foreignKeys;

    public MissingParentEntitiesFilter(Collection<EntityField<E, ?>> foreignKeys) {
        this.foreignKeys = foreignKeys;
    }

    @Override
    public <T extends EntityChange<E>> Collection<T> filter(Collection<T> changes, ChangeOperation changeOperation, ChangeContext changeContext) {
        if (foreignKeys.isEmpty()) {
            return changes;
        }
        return Collections2.filter(changes, command -> {
                    Entity entity = changeContext.getEntity(command);
                    if (entity == Entity.EMPTY) {
                        changeContext.addValidationError(command, new ValidationError(Errors.PARENT_ENTITY_NOT_FOUND));
                        return false;
                    }
                    return true;
                }
        );
    }

    @Override
    public Stream<EntityField<?, ?>> getRequiredFields(Collection<? extends ChangeEntityCommand<E>> changeEntityCommands, ChangeOperation changeOperation) {
        return Stream.of();
    }

    @Override
    public SupportedChangeOperation getSupportedChangeOperation() {
        return SupportedChangeOperation.CREATE;
    }
}
