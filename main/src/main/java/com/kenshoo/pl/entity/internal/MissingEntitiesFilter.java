package com.kenshoo.pl.entity.internal;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.kenshoo.pl.entity.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

public class MissingEntitiesFilter<E extends EntityType<E>> implements ChangesFilter<E> {

    @Override
    public <T extends EntityChange<E>> Collection<T> filter(Collection<T> changes, ChangeOperation changeOperation, ChangeContext changeContext) {
        return Collections2.filter(changes, command -> {
                    Entity entity = changeContext.getEntity(command);
                    if (entity == Entity.EMPTY) {
                        if(!command.allowMissingEntity()) {
                            changeContext.addValidationError(command, new ValidationError(Errors.ENTITY_NOT_FOUND, ImmutableMap.of("id", command.getIdentifier().toString())));
                        }
                        return false;
                    }
                    return true;
                }
        );
    }

    @Override
    public Stream<EntityField<?, ?>> getRequiredFields(Collection<? extends ChangeEntityCommand<E>> changeEntityCommands, ChangeOperation changeOperation) {
        if (changeEntityCommands.isEmpty()) {
            return Stream.of();
        } else {
            return Arrays.stream(changeEntityCommands.stream().findFirst().get().getIdentifier().getUniqueKey().getFields());
        }
    }

    @Override
    public SupportedChangeOperation getSupportedChangeOperation() {
        return SupportedChangeOperation.UPDATE_AND_DELETE;
    }
}
